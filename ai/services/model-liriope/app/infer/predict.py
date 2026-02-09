import time
import numpy as np
from io import BytesIO
from PIL import Image
import tensorflow as tf

from app.infer.preprocess import preprocess_pil
from app.infer.labels_liriope import CLASSES, to_korean
from app.infer.loader import MODEL_ID

# =========================
# crop_id -> 허용 prefix 임시 매핑 (나중에 DB 확정되면 교체)
# =========================
CROP_ALLOWED_PREFIXES = {
    2: ["Apple___"],     # 사과
    3: ["Tomato___"],    # 토마토
    101: ["Rice___"],    # (liriope엔 rice 없음) 예시
    102: ["Potato___"],  # 감자
    103: ["Corn_(maize)___"],  # 옥수수
    104: ["Wheat___"],   # (liriope엔 wheat 없음) 예시
}

INVALID_LABEL_EN = "Invalid"
INVALID_LABEL_KO = "결과 없음"

def _softmax(x: np.ndarray) -> np.ndarray:
    x = x.astype(np.float64)
    x = x - np.max(x)
    e = np.exp(x)
    return (e / (e.sum() + 1e-12)).astype(np.float32)

def _ensure_prob_vector(raw: np.ndarray) -> np.ndarray:
    p = raw.astype(np.float32)
    s = float(np.sum(p))
    if not np.isfinite(s) or s <= 0:
        return _softmax(p)
    if abs(s - 1.0) > 0.05:
        return _softmax(p)
    if np.min(p) < 0:
        return _softmax(p)
    return p

def confidence_metrics(probs: np.ndarray) -> dict:
    p = probs.astype(np.float64)
    s = float(p.sum())
    if not np.isfinite(s) or s <= 0:
        p = np.ones_like(p, dtype=np.float64) / len(p)
    else:
        p = p / (s + 1e-12)

    idx = np.argsort(p)[::-1]
    p1, p2 = float(p[idx[0]]), float(p[idx[1]])
    margin = p1 - p2
    entropy = float(-(p * np.log(p + 1e-12)).sum())
    return {
        "top1_index": int(idx[0]),
        "top2_index": int(idx[1]),
        "top1_prob": p1,
        "top2_prob": p2,
        "margin": margin,
        "entropy": entropy,
    }

def _invalid_result(meta: dict, top_k: int = 5) -> dict:
    # topk도 동일하게 Invalid로 채우면 프론트가 처리 편함
    items = [{
        "label": INVALID_LABEL_EN,
        "label_ko": INVALID_LABEL_KO,
        "prob": 1.0,
        "index": -1,
    }]
    return {
        "model": MODEL_ID,
        "best": items[0],
        "topk": items[:max(1, min(top_k, 10))],
        "confidence": {
            "top1_index": -1,
            "top2_index": -1,
            "top1_prob": 0.0,
            "top2_prob": 0.0,
            "margin": 0.0,
            "entropy": 0.0,
        },
        "meta": meta,
    }

def _apply_crop_filter(probs: np.ndarray, crop_id: int | None) -> tuple[np.ndarray, dict]:
    """
    crop_id에 맞는 클래스만 남기고 나머지는 0으로 마스킹.
    남은 확률합이 너무 작으면 Invalid로 처리하기 위해 kept_sum을 리턴.
    """
    meta = {"crop_id": crop_id, "filtered": False, "kept_sum": None, "allowed_prefixes": None}

    if crop_id is None:
        return probs, meta

    prefixes = CROP_ALLOWED_PREFIXES.get(int(crop_id))
    if not prefixes:
        # crop_id를 받았지만 매핑이 없으면: 필터 안 함(또는 정책상 REJECT로 할 수도 있음)
        meta["filtered"] = False
        return probs, meta

    keep_mask = np.zeros_like(probs, dtype=np.float32)
    for i, label in enumerate(CLASSES):
        if any(label.startswith(pfx) for pfx in prefixes):
            keep_mask[i] = 1.0

    masked = probs * keep_mask
    kept_sum = float(masked.sum())

    meta["filtered"] = True
    meta["kept_sum"] = kept_sum
    meta["allowed_prefixes"] = prefixes

    # 재정규화(합이 0이면 그대로 반환)
    if kept_sum > 1e-8:
        masked = masked / kept_sum

    return masked.astype(np.float32), meta

def topk_items(probs: np.ndarray, top_k: int) -> list[dict]:
    top_k = max(1, min(int(top_k), len(probs)))
    idxs = np.argsort(probs)[::-1][:top_k]
    out = []
    for i in idxs:
        label_en = CLASSES[i] if i < len(CLASSES) else f"class_{i}"
        out.append({
            "label": label_en,
            "label_ko": to_korean(label_en),
            "prob": float(probs[i]),
            "index": int(i),
        })
    return out

def predict_single(
    model: tf.keras.Model,
    img: Image.Image,
    prep_mode="v3",
    resize_mode="pad",
    crop_ratio=None,
    use_leaf_mask=False,
    image_size=(380, 380),
) -> np.ndarray:
    x = preprocess_pil(
        img,
        image_size=image_size,
        prep_mode=prep_mode,
        resize_mode=resize_mode,
        crop_ratio=crop_ratio,
        use_leaf_mask=use_leaf_mask,
    )
    raw = model.predict(x, verbose=0)[0]
    return _ensure_prob_vector(raw)

def predict_ensemble(
    model: tf.keras.Model,
    img: Image.Image,
    prep_mode="v3",
    resize_mode="pad",
    crop_candidates=None,
    use_leaf_mask=False,
    image_size=(380, 380),
) -> np.ndarray:
    if crop_candidates is None:
        crop_candidates = [None, 0.95, 0.90]

    probs_list = []
    for cr in crop_candidates:
        probs_list.append(
            predict_single(
                model,
                img,
                prep_mode=prep_mode,
                resize_mode=resize_mode,
                crop_ratio=cr,
                use_leaf_mask=use_leaf_mask,
                image_size=image_size,
            )
        )
    avg = np.mean(np.stack(probs_list, axis=0), axis=0)
    return avg.astype(np.float32)

def predict_from_bytes(
    model: tf.keras.Model,
    image_bytes: bytes,
    top_k: int = 5,
    prep_mode: str = "v3",
    resize_mode: str = "pad",
    crop_ratio: float | None = None,
    use_ensemble: bool = False,
    ensemble_crops: list[float | None] | None = None,
    use_leaf_mask: bool = False,
    image_size=(380, 380),
    crop_id: int | None = None,

    # Invalid 판정 기준(필요 시 조정)
    reject_kept_sum: float = 0.20,   # 필터 후 남는 확률 합이 이보다 작으면 "결과 없음"
    reject_top1_prob: float = 0.50,  # 필터 후 top1이 이보다 작아도 "결과 없음"
) -> dict:
    t0 = time.time()
    img = Image.open(BytesIO(image_bytes)).convert("RGB")

    if use_ensemble:
        probs = predict_ensemble(
            model,
            img,
            prep_mode=prep_mode,
            resize_mode=resize_mode,
            crop_candidates=ensemble_crops,
            use_leaf_mask=use_leaf_mask,
            image_size=image_size,
        )
    else:
        probs = predict_single(
            model,
            img,
            prep_mode=prep_mode,
            resize_mode=resize_mode,
            crop_ratio=crop_ratio,
            use_leaf_mask=use_leaf_mask,
            image_size=image_size,
        )

    # crop_id 필터 적용
    probs_f, filter_meta = _apply_crop_filter(probs, crop_id=crop_id)

    # Invalid 판정 (필터 적용된 경우에만 강하게)
    # kept_sum이 너무 낮으면 해당 crop prefix 계열로는 설명이 안 되는 이미지라는 뜻
    if filter_meta.get("filtered"):
        kept_sum = float(filter_meta.get("kept_sum") or 0.0)
        top1_prob = float(np.max(probs_f)) if probs_f.size else 0.0
        if (kept_sum < reject_kept_sum) or (top1_prob < reject_top1_prob):
            elapsed_ms = int((time.time() - t0) * 1000)
            meta = {
                "latency_ms": elapsed_ms,
                "prep_mode": prep_mode,
                "resize_mode": resize_mode,
                "crop_ratio": crop_ratio,
                "use_ensemble": use_ensemble,
                "ensemble_crops": ensemble_crops,
                "use_leaf_mask": use_leaf_mask,
                "image_size": list(image_size),

                # 필터 정보도 같이 내려주기(디버깅)
                **filter_meta,
                "reject_kept_sum": reject_kept_sum,
                "reject_top1_prob": reject_top1_prob,
            }
            return _invalid_result(meta=meta, top_k=top_k)

    conf = confidence_metrics(probs_f)
    items = topk_items(probs_f, top_k)

    elapsed_ms = int((time.time() - t0) * 1000)
    best = items[0]

    return {
        "model": MODEL_ID,
        "best": best,
        "topk": items,
        "confidence": conf,
        "meta": {
            "latency_ms": elapsed_ms,
            "prep_mode": prep_mode,
            "resize_mode": resize_mode,
            "crop_ratio": crop_ratio,
            "use_ensemble": use_ensemble,
            "ensemble_crops": ensemble_crops,
            "use_leaf_mask": use_leaf_mask,
            "image_size": list(image_size),

            # 필터 정보 내려주기
            **filter_meta,
            "reject_kept_sum": reject_kept_sum,
            "reject_top1_prob": reject_top1_prob,
        },
    }
