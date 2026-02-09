from typing import Optional, List, Dict, Any, Tuple
from langchain_community.vectorstores import Chroma

from app.core.config import (
    PERSIST_DIR,
    COLLECTION_CULTIVATION,
    COLLECTION_DISEASE,
    COLLECTION_DIAGNOSIS,
    EMB_DEVICE,
)
from app.embed.model import load_embeddings

_vectorstores: Optional[Dict[str, Chroma]] = None


def load_vectorstores(device: Optional[str] = None) -> Dict[str, Chroma]:
    global _vectorstores
    if _vectorstores is not None:
        return _vectorstores

    if device is None:
        device = EMB_DEVICE

    embeddings = load_embeddings(device)

    cultivation = Chroma(
        collection_name=COLLECTION_CULTIVATION,
        embedding_function=embeddings,
        persist_directory=PERSIST_DIR,
    )
    disease = Chroma(
        collection_name=COLLECTION_DISEASE,
        embedding_function=embeddings,
        persist_directory=PERSIST_DIR,
    )
    diagnosis = Chroma(
        collection_name=COLLECTION_DIAGNOSIS,
        embedding_function=embeddings,
        persist_directory=PERSIST_DIR,
    )

    _vectorstores = {"cultivation": cultivation, "disease": disease, "diagnosis": diagnosis}
    return _vectorstores


def _unique_docs_by_content(items: List[Tuple[Any, float]]):
    seen = set()
    out = []
    for doc, score in items:
        key = (doc.page_content or "").strip()
        if not key or key in seen:
            continue
        seen.add(key)
        out.append((doc, score))
    return out


def query_evidences(question: str, intent: str, k: int = 4) -> List[Dict[str, Any]]:
    vs = load_vectorstores()

    if intent == "재배법":
        docs_scores = vs["cultivation"].similarity_search_with_score(question, k=k)

    elif intent == "병충해":
        docs_scores = vs["disease"].similarity_search_with_score(question, k=k)
    
    elif intent == "진단":
        docs_scores = vs["diagnosis"].similarity_search_with_score(question, k=k)

    elif intent == "둘 다":
        a = vs["cultivation"].similarity_search_with_score(question, k=2)
        b = vs["disease"].similarity_search_with_score(question, k=2)
        docs_scores = _unique_docs_by_content(a + b)[:k]

    else:  # "해당없음"
        return []

    evidences = []
    for doc, score in docs_scores:
        evidences.append({
            "content": doc.page_content,
            "metadata": doc.metadata or {},
            "score": float(score) if score is not None else None,
        })
    return evidences
