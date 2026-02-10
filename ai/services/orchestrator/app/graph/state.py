from __future__ import annotations
from typing import Any, Dict, List, Optional, TypedDict, Literal


Decision = Literal["CONFIDENT", "UNCERTAIN", "REJECT"]
ModelName = Literal["liriope", "wambugu"]

class ModelTopKItem(TypedDict, total=False):
    index: int
    label: str
    label_ko: str
    prob: float

class ModelPredictResponse(TypedDict, total=False):
    model: str
    best: ModelTopKItem
    topk: List[ModelTopKItem]
    confidence: Dict[str, Any]
    meta: Dict[str, Any]

class EvidenceDoc(TypedDict, total=False):
    doc_id: str
    title: str
    source: str
    score: float
    snippet: str

class AnalyzeState(TypedDict, total=False):
    # input
    request_id: str
    crop_id: int
    target_model: ModelName
    image_bytes: bytes
    image_mime: str
    top_k: int
    thresholds: Dict[str, float]
    routing: Dict[str, Any]

    # outputs (intermediate)
    liriope: Dict[str, Any]
    wambugu: Dict[str, Any]

    # final outputs
    decision: Decision
    final: Dict[str, Any]
    evidence: List[Dict[str, Any]]
    meta: Dict[str, Any]
    explanation: Dict[str, Any]

class ChatState(TypedDict, total=False):
    conversation_id: str
    user_message: str
    # context from memory
    diagnosis: Dict[str, Any]   # analyze final
    history: List[Dict[str, str]]
    # retrieval
    evidence: List[EvidenceDoc]
    # llm output
    answer: str
    followups: List[str]
    meta: Dict[str, Any]
