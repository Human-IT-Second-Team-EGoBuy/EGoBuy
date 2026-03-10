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


async def run_model(state: AnalyzeState) -> AnalyzeState:
    result = await predict_model(
        image_bytes=state["image_bytes"],
        image_mime=state["image_mime"],
        top_k=int(state.get("top_k", 5)),
        crop_id=state.get("crop_id"),
    )
    state["model_result"] = result
    return state


def _score_from_result(result: Dict[str, Any], topk: List[Dict[str, Any]]) -> Tuple[float, float]:
    conf = result.get("confidence")
    if isinstance(conf, dict):
        try:
            return float(conf.get("top1_prob", 0.0)), float(conf.get("margin", 0.0))
        except Exception:
            pass
    return _score_from_topk(topk)

def _score_from_topk_global(topk: List[Dict[str, Any]]) -> Tuple[float, float]:
    """
    top1_prob_global, margin_global(top1-top2)
    topk 항목에 prob_global이 있을 때만 의미 있음.
    """
    def prob_g(item: Dict[str, Any]) -> float:
        try:
            return float(item.get("prob_global", 0.0))
        except Exception:
            return 0.0

    if not topk:
        return 0.0, 0.0

    p1 = prob_g(topk[0])
    p2 = prob_g(topk[1]) if len(topk) > 1 else 0.0
    return p1, (p1 - p2)


async def decide_and_finalize(state: AnalyzeState) -> AnalyzeState:
    result = state.get("model_result", {}) or {}

    topk = _extract_topk(result)

    # 1) 기존 점수(보통: crop 필터링이면 조건부(prob), 아니면 global(prob))
    p1_cond, margin_cond = _score_from_result(result, topk)

    # 2) global 점수(prob_global 기반)
    p1_g, margin_g = _score_from_topk_global(topk)

    thresholds = state.get("thresholds", {}) or {}

    # 필터링 여부 (model-service가 meta.filtered_by_crop 내려주는 경우)
    meta = result.get("meta") or {}
    filtered = bool(meta.get("filtered_by_crop"))

    # 하이브리드: "REJECT만 global로 가드"
    # (테스트 전이라 보수적으로 기본값 0.15 추천. 필요하면 0.10~0.20에서 튜닝)
    reject_min_prob_global = float(thresholds.get("reject_min_prob_global", 0.15))

    if filtered and (p1_g > 0.0):
        # 1) global이 너무 낮으면, 조건부가 높아도 거절(과확신 방지)
        if p1_g < reject_min_prob_global:
            decision = "REJECT"
        else:
            # 2) confident/uncertain은 기존 기준(조건부) 그대로
            decision = _decide(p1_cond, margin_cond, thresholds)
    else:
        # 필터링이 아니거나 prob_global이 없으면 기존 로직 그대로
        decision = _decide(p1_cond, margin_cond, thresholds)

    best = topk[0] if topk else {}
    label_en = best.get("label") or best.get("class") or best.get("name") or ""
    label_ko = best.get("label_ko") or ""

    if label_en == "Invalid":
        decision = "REJECT"
        label_ko = "결과 없음"

    final = {
        # UI/기존 호환용: 기존 필드 유지(조건부 기준)
        "top1_prob": p1_cond,
        "margin": margin_cond,
        "label": label_en,
        "label_ko": label_ko,
        "raw": best,

        # 디버깅/튜닝용 추가 필드
        "top1_prob_global": p1_g,
        "margin_global": margin_g,
        "filtered_by_crop": filtered,
        "reject_min_prob_global": reject_min_prob_global,
        "score_basis": "hybrid(reject=global,conf=cond)" if (filtered and p1_g > 0.0) else "default",
    }

    state["decision"] = decision
    state["final"] = final
    state.setdefault("meta", {})
    state["meta"]["top1_prob"] = p1_cond
    state["meta"]["margin"] = margin_cond
    state["meta"]["top1_prob_global"] = p1_g
    state["meta"]["margin_global"] = margin_g
    state["meta"]["score_basis"] = final["score_basis"]
    return state