import time
from typing import List, Optional, Literal, Dict, Any

from fastapi import APIRouter, UploadFile, File, HTTPException, Form
from pydantic import BaseModel, Field

from app.graph.builder import get_analyze_graph
from app.storage.memory_store import new_conversation_id

from app.services.retriever import retrieve
from app.services.diagnosis_rag_llm import (
    get_llm as get_diag_llm,
    build_diagnosis_prompt,
)

from app.services.intent import classify_intent
from app.services.llm import get_llm as get_chat_llm, build_chat_prompt


router = APIRouter()
MAX_BYTES = 10 * 1024 * 1024


@router.get("/health")
def health():
    return {"ok": True, "service": "orchestrator"}


@router.post("/analyze")
async def analyze(
    image: UploadFile = File(...),
    crop_id: int = Form(...),
    top_k: int = Form(5),
):
    # 1) 파일 검증
    if not image.content_type or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="image/* 파일만 허용")

    img_bytes = await image.read()
    if not img_bytes:
        raise HTTPException(status_code=400, detail="빈 파일")

    if len(img_bytes) > MAX_BYTES:
        raise HTTPException(status_code=413, detail="파일이 너무 큽니다(10MB 제한)")

    # 2) LangGraph analyze 실행
    t0 = time.time()
    graph = get_analyze_graph()
    conv_id = new_conversation_id()

    state_in: Dict[str, Any] = {
        "request_id": conv_id,
        "crop_id": crop_id,
        "image_bytes": img_bytes,
        "image_mime": image.content_type,
        "top_k": int(top_k),
        "thresholds": {
            "confident_min_prob": 0.65,
            "confident_min_margin": 0.15,
            "reject_min_prob": 0.35,
            # nodes_analyze.py 하이브리드 가드에서 사용(없으면 기본 0.15)
            # "reject_min_prob_global": 0.15,
        },
        # graph 내부 LLM 사용 여부(너희 설계 유지)
        "llm": {"enabled": False},
    }

    out = await graph.ainvoke(state_in)

    out.setdefault("meta", {})
    out["meta"]["latency_ms_total"] = int((time.time() - t0) * 1000)

    # 3) target_model 추출 + final에 심기 (Spring DTO 안정)
    model_result = out.get("model_result") or {}
    target_model = None
    if isinstance(model_result, dict):
        target_model = model_result.get("model")

    final = out.get("final", {}) or {}
    if isinstance(final, dict) and target_model:
        final["target_model"] = target_model

    # 4) (진단 RAG) — label_ko가 있고, REJECT가 아니면 수행 (비용/혼선 방지)
    decision = out.get("decision")
    label_ko = (final.get("label_ko") or "").strip()

    rag_answer = None
    rag_evidence = []

    if label_ko and decision != "REJECT":
        rag_evidence = await retrieve(query=label_ko, intent="진단", top_n=5)
        prompt = build_diagnosis_prompt(label_ko=label_ko, evidence_docs=rag_evidence)
        llm = get_diag_llm()
        resp = llm.invoke(prompt)
        rag_answer = resp.content if hasattr(resp, "content") else str(resp)

    return {
        "conversation_id": conv_id,
        "crop_id": crop_id,
        "target_model": target_model,   
        "decision": decision,
        "model_result": out.get("model_result"),
        "final": final,                 
        "evidence": out.get("evidence"),
        "meta": out.get("meta"),
        "rag_answer": rag_answer,
        "rag_evidence": rag_evidence,
    }


class HistoryMessage(BaseModel):
    role: Literal["USER", "ASSISTANT"]
    content: str

class ChatRagRequest(BaseModel):
    message: str
    history: List[HistoryMessage] = Field(default_factory=list)  # 없으면 []
    chatroom_id: Optional[int] = None
    crop_id: Optional[int] = None


@router.post("/chat_rag")
async def chat_rag(req: ChatRagRequest):
    intent = classify_intent(req.message)

    if intent == "해당없음":
        return {
            "answer": "농업(병충해/재배법) 질문인지 확인이 필요해요. 작물/상황을 조금만 더 알려주세요.",
            "intent": intent,
            "citations": [],
        }

    evidence = await retrieve(req.message, intent=intent, top_n=5)

    history_dicts = [m.model_dump() for m in req.history]

    prompt = build_chat_prompt(
        user_message=req.message,
        evidence_docs=evidence,
        history=history_dicts,
    )

    llm = get_chat_llm()
    resp = llm.invoke(prompt)
    answer = resp.content if hasattr(resp, "content") else str(resp)

    return {"answer": answer, "intent": intent, "citations": evidence}
