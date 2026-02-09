# app/infer/predict.py
import time
from io import BytesIO
from PIL import Image
import torch

from app.infer.loader import REPO_ID
from app.infer.labels_wambugu import to_korean
from app.infer.preprocess import preprocess_pil

def _confidence_metrics(probs: torch.Tensor) -> dict:
    """
    probs: (num_classes,) on CPU or GPU
    """
    p = probs.detach().float()
    # 안전하게 CPU로
    pcpu = p.cpu()

    topk = torch.topk(pcpu, k=min(2, pcpu.numel()))
    p1 = float(topk.values[0])
    p2 = float(topk.values[1]) if topk.values.numel() > 1 else 0.0
    margin = p1 - p2

    # entropy
    eps = 1e-12
    entropy = float(-(pcpu * (pcpu + eps).log()).sum())

    return {
        "top1_prob": p1,
        "top2_prob": p2,
        "margin": margin,
        "entropy": entropy,
    }

def predict_from_bytes(
    model,
    processor,
    device,
    image_bytes: bytes,
    top_k: int = 5,
) -> dict:
    t0 = time.time()
    img = Image.open(BytesIO(image_bytes)).convert("RGB")

    inputs = preprocess_pil(processor, img, device)

    with torch.no_grad():
        outputs = model(**inputs)

    logits = outputs.logits[0]  # (C,)
    probs = torch.softmax(logits, dim=-1)  # (C,)

    k = max(1, min(int(top_k), probs.numel()))
    topk_probs, topk_indices = torch.topk(probs, k=k)

    # id2label
    id2label = model.config.id2label or {}
    items = []
    for p, idx in zip(topk_probs, topk_indices):
        idx_i = int(idx.item())
        label = id2label.get(idx_i, f"class_{idx_i}")
        items.append({
            "index": idx_i,
            "label": label,
            "label_ko": to_korean(label), 
            "prob": float(p.item()),
        })

    conf = _confidence_metrics(probs)

    elapsed_ms = int((time.time() - t0) * 1000)
    return {
        "model": REPO_ID,
        "best": items[0] if items else None,
        "topk": items,
        "confidence": conf,
        "meta": {
            "latency_ms": elapsed_ms,
            "device": str(device),
        }
    }
