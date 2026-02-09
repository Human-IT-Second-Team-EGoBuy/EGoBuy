from typing import Any, Dict, List, Tuple

from app.graph.state import AnalyzeState
from app.services.model_client import predict_model





def _extract_topk(result: Dict[str, Any]) -> List[Dict[str, Any]]:
    """
    모델 서비스 응답에서 topk 리스트를 뽑아내는 함수.
    너 모델 서비스가 어떤 형태로 주는지 100% 확정이 없어서
    흔히 쓰는 키들을 순서대로 시도함.
    """
    for key in ("topk", "predictions", "results"):
        v = result.get(key)
        if isinstance(v, list):
            return v
    return []


def _score_from_topk(topk: List[Dict[str, Any]]) -> Tuple[float, float]:
    """
    top1_prob, margin(top1-top2)
    topk 항목의 확률 키도 여러가지일 수 있어(prob, score, confidence).
    """
    def prob(item: Dict[str, Any]) -> float:
        for k in ("prob", "probability", "score", "confidence"):
            if k in item:
                try:
                    return float(item[k])
                except Exception:
                    pass
        return 0.0

    if not topk:
        return 0.0, 0.0

    p1 = prob(topk[0])
    p2 = prob(topk[1]) if len(topk) > 1 else 0.0
    return p1, (p1 - p2)


def _decide(p1: float, margin: float, thresholds: Dict[str, float]) -> str:
    confident_min_prob = float(thresholds.get("confident_min_prob", 0.65))
    confident_min_margin = float(thresholds.get("confident_min_margin", 0.15))
    reject_min_prob = float(thresholds.get("reject_min_prob", 0.35))

    if p1 < reject_min_prob:
        return "REJECT"
    if (p1 >= confident_min_prob) and (margin >= confident_min_margin):
        return "CONFIDENT"
    return "UNCERTAIN"


async def run_selected_model(state: AnalyzeState) -> AnalyzeState:
    """
      target_model만 호출해서 state["liriope"] 또는 state["wambugu"]에 저장
    """
    target = state["target_model"]
    result = await predict_model(
        model_name=target,
        image_bytes=state["image_bytes"],
        image_mime=state["image_mime"],
        top_k=int(state.get("top_k", 5)),
        crop_id=state.get("crop_id"),
    )
    state[target] = result
    return state


def _score_from_result(result: Dict[str, Any], topk: List[Dict[str, Any]]) -> Tuple[float, float]:
    conf = result.get("confidence")
    if isinstance(conf, dict):
        try:
            return float(conf.get("top1_prob", 0.0)), float(conf.get("margin", 0.0))
        except Exception:
            pass
    return _score_from_topk(topk)


async def decide_and_finalize(state: AnalyzeState) -> AnalyzeState:
    target = state["target_model"]
    result = state.get(target, {}) or {}

    topk = _extract_topk(result)
    p1, margin = _score_from_result(result, topk)  # confidence 우선

    thresholds = state.get("thresholds", {}) or {}
    decision = _decide(p1, margin, thresholds)

    best = topk[0] if topk else {}
    label_en = best.get("label") or best.get("class") or best.get("name") or ""
    label_ko = best.get("label_ko") or ""

    # meta 기반 추가 reject (필터 후 후보합이 너무 작으면 지원불가로 처리)
    meta = result.get("meta") or {}
    if isinstance(meta, dict) and meta.get("filtered") is True:
        try:
            kept_sum = float(meta.get("kept_sum", 0.0))
            reject_kept_sum = float(meta.get("reject_kept_sum", 0.2))
            if kept_sum < reject_kept_sum:
                label_en = "Invalid"
        except Exception:
            pass

    if label_en == "Invalid":
        decision = "REJECT"
        label_ko = "결과 없음"

    best_with_ko = dict(best)
    if label_ko:
        best_with_ko["label_ko"] = label_ko

    final = {
        "target_model": target,
        "top1_prob": p1,
        "margin": margin,
        "label": label_en,
        "label_ko": label_ko,
        "raw": best_with_ko,
    }

    state["decision"] = decision  # type: ignore
    state["final"] = final
    state.setdefault("evidence", [])
    state.setdefault("meta", {})
    state["meta"]["top1_prob"] = p1
    state["meta"]["margin"] = margin
    return state