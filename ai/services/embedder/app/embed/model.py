from typing import Optional
from langchain_huggingface import HuggingFaceEmbeddings

from app.core.config import EMB_DEVICE, EMB_MODEL_NAME, EMB_BATCH_SIZE

_embeddings: Optional[HuggingFaceEmbeddings] = None

def load_embeddings(device: Optional[str] = None) -> HuggingFaceEmbeddings:
    global _embeddings
    if _embeddings is not None:
        return _embeddings

    if device is None:
        device = EMB_DEVICE

    _embeddings = HuggingFaceEmbeddings(
        model_name=EMB_MODEL_NAME,
        model_kwargs={"device": device},
        encode_kwargs={"normalize_embeddings": True, "batch_size": EMB_BATCH_SIZE},
    )
    return _embeddings
