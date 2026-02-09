import os
MODEL_DIR = os.getenv("MODEL_DIR", "/models/wambugu")

# torch import 전에 설정해야 함 (윈도우/중복 라이브러리/스레드 이슈 완화)
os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")
os.environ.setdefault("OMP_NUM_THREADS", "1")
os.environ.setdefault("MKL_NUM_THREADS", "1")

import torch
from transformers import ViTImageProcessor, ViTForImageClassification

REPO_ID = "wambugu71/crop_leaf_diseases_vit"

_MODEL = None
_PROCESSOR = None
_DEVICE = None

def get_device() -> torch.device:
    global _DEVICE
    if _DEVICE is None:
        _DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    return _DEVICE

def get_processor() -> ViTImageProcessor:
    global _PROCESSOR
    if _PROCESSOR is None:
        _PROCESSOR = ViTImageProcessor.from_pretrained(REPO_ID)
    return _PROCESSOR

def get_model() -> ViTForImageClassification:
    global _MODEL
    if _MODEL is not None:
        return _MODEL

    device = get_device()
    model = ViTForImageClassification.from_pretrained(
        REPO_ID,
        ignore_mismatched_sizes=True,
    )
    model.eval()
    model.to(device)

    _MODEL = model
    return _MODEL

def warmup() -> None:
    """
    서버 시작 시 1회 워밍업(첫 추론 지연 완화).
    """
    import torch
    model = get_model()
    processor = get_processor()
    device = get_device()

    # 더미 입력(224x224 RGB)
    dummy = torch.zeros((1, 3, 224, 224), dtype=torch.float32, device=device)
    with torch.no_grad():
        _ = model(pixel_values=dummy)
