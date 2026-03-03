# app/infer/predict.py
from __future__ import annotations

import time
from io import BytesIO
from typing import Any, Dict, List

import torch
from PIL import Image
from torchvision import transforms

from app.infer.labels import CLASSES, to_korean, allowed_indices_for_crop_id
from app.infer.preprocess import preprocess_pil


_EVAL_TFM = transforms.Compose([
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


@torch.inference_mode()
def predict_from_bytes(
    processor,                 
    device: torch.device,
    image_bytes: bytes,
    top_k: int = 5,
    crop_id: int | None = None,   
) -> Dict[str, Any]:
    t0 = time.time()

    img = Image.open(BytesIO(image_bytes))

    # EXIF transpose + resize(224) + RGB 안전 처리
    img = preprocess_pil(img, image_size=(224, 224), resize_mode="stretch")

    x = _EVAL_TFM(img).unsqueeze(0).to(device)

    logits = processor(x)  
    if logits.ndim == 2:
        logits = logits[0]  

    # 전체(52클래스) 기준 확률
    probs_global = torch.softmax(logits, dim=0)

    allowed = allowed_indices_for_crop_id(crop_id)
    filtered = bool(allowed)

    if filtered:
        idx_tensor = torch.tensor(allowed, device=logits.device, dtype=torch.long)
        sub_logits = logits.index_select(0, idx_tensor)     # (C_sub,)
        sub_probs = torch.softmax(sub_logits, dim=0)        # 재정규화(조건부 softmax)

        k = max(1, min(int(top_k), int(sub_probs.numel())))
        top_probs, top_pos = torch.topk(sub_probs, k=k)
        top_idx = idx_tensor.index_select(0, top_pos)       # sub idx → global idx 복원
    else:
        k = max(1, min(int(top_k), len(CLASSES)))
        top_probs, top_idx = torch.topk(probs_global, k=k)

    topk: List[Dict[str, Any]] = []
    for r in range(k):
        idx = int(top_idx[r].item())
        p = float(top_probs[r].item())
        label_en = CLASSES[idx]
        topk.append({
            "index": idx,
            "label": label_en,
            "label_ko": to_korean(label_en),
            # filtered=True면 “작물 후보군 내 조건부 확률”
            "prob": p,
            # 전체 52클래스 기준 확률도 같이 내려주면 튜닝/판단에 도움됨
            "prob_global": float(probs_global[idx].item()),
        })

    best = topk[0]
    p1 = float(best["prob"])
    p2 = float(topk[1]["prob"]) if len(topk) > 1 else 0.0
    margin = p1 - p2

    return {
        "model": "efficientnet_b2",
        "best": best,
        "topk": topk,
        "confidence": {"top1_prob": p1, "margin": margin},
        "meta": {
            "latency_ms": int((time.time() - t0) * 1000),
            "crop_id": crop_id,
            "filtered_by_crop": filtered,
            "allowed_count": (len(allowed) if allowed else None),
        },
    }