import os
from typing import List, Dict, Any, Literal
import httpx

EMBEDDER_BASE_URL = os.getenv("EMBEDDER_BASE_URL", "http://localhost:8030")

Intent = Literal["재배법", "병충해", "진단", "둘 다", "해당없음"]

async def retrieve(query: str, intent: Intent = "둘 다", top_n: int = 5) -> List[Dict[str, Any]]:
    """
    embedder(/query) 호출해서 evidences 받아오고
    기존 EvidenceDoc 형태로 변환해서 반환
    """
    url = f"{EMBEDDER_BASE_URL}/query"
    payload = {"query": query, "intent": intent, "k": top_n}

    async with httpx.AsyncClient(timeout=30.0) as client:
        r = await client.post(url, json=payload)
        r.raise_for_status()
        data = r.json()

    evidences = data.get("evidences", []) or []
    docs: List[Dict[str, Any]] = []

    for i, ev in enumerate(evidences):
        md = ev.get("metadata") or {}
        content = ev.get("content") or ""
        score = ev.get("score")

        docs.append({
            "doc_id": str(md.get("doc_id") or md.get("id") or f"ev_{i}"),
            "title": md.get("title", ""),
            "source": md.get("source", ""),
            "score": float(score) if score is not None else 0.0,
            "snippet": (content[:500] + "…") if len(content) > 500 else content,
            # 필요하면 원문도 달아둘 수 있음
            # "content": content,
            # "metadata": md,
        })

    return docs
