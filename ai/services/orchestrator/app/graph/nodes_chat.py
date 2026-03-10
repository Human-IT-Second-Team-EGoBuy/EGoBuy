import time
from app.graph.state import ChatState
from app.services.retriever import retrieve
from app.services.llm import get_llm, build_chat_prompt

from langgraph.graph import StateGraph, END


async def node_retrieve_for_chat(state: ChatState) -> ChatState:
    diagnosis = state.get("diagnosis", {}) or {}
    label_ko = (diagnosis.get("label_ko") or "").strip()
    crop_id = diagnosis.get("crop_id")
    user_message = state["user_message"]

    parts = []
    if crop_id is not None:
        parts.append(f"crop_id:{crop_id}")
    if label_ko:
        parts.append(label_ko)
    parts.append(user_message)

    q = " ".join(parts)

    # 진단 후속 질문용이면 병충해만 강제하고 싶을 수도 있음(정책상 어차피 안 쓰면 상관 없음)
    state["evidence"] = await retrieve(q, intent="병충해", top_n=5)
    return state



def node_generate_answer(state: ChatState) -> ChatState:
    t0 = time.time()
    llm = get_llm()

    history = (state.get("history", []) or [])[-10:]

    prompt = build_chat_prompt(
        user_message=state["user_message"],
        diagnosis=state.get("diagnosis", {}) or {},
        evidence_docs=state.get("evidence", []) or [],
        history=history,
    )

    try:
        resp = llm.invoke(prompt)
        text = resp.content if hasattr(resp, "content") else str(resp)
    except Exception as e:
        text = "지금 답변 생성에 실패했습니다. 잠시 후 다시 시도해 주세요."
        state.setdefault("meta", {})
        state["meta"]["llm_error"] = str(e)

    state["answer"] = text
    state.setdefault("meta", {})
    state["meta"]["latency_ms_llm"] = int((time.time() - t0) * 1000)
    return state


def build_chat_graph():
    g = StateGraph(ChatState)

    #  state key와 겹치지 않는 노드명 사용
    g.add_node("retrieve_ctx", node_retrieve_for_chat)
    g.add_node("generate_answer", node_generate_answer)

    g.set_entry_point("retrieve_ctx")
    g.add_edge("retrieve_ctx", "generate_answer")
    g.add_edge("generate_answer", END)

    return g.compile()


#  builder.py가 import 하는 이름 유지
def chat_flow():
    return build_chat_graph()
