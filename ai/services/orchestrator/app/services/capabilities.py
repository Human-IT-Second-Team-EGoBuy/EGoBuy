import json
from pathlib import Path
from typing import Literal, Optional, Dict, Any

ModelName = Literal["liriope", "wambugu"]

_BASE_DIR = Path(__file__).resolve().parents[1]  # app/
_CAP_PATH = _BASE_DIR / "config" / "capabilities.json"

_CAP_CACHE: Optional[Dict[str, Any]] = None


def load_capabilities() -> Dict[str, Any]:
    global _CAP_CACHE
    if _CAP_CACHE is None:
        with open(_CAP_PATH, "r", encoding="utf-8") as f:
            _CAP_CACHE = json.load(f)
    return _CAP_CACHE


def route_model_by_crop_id(crop_id: int) -> ModelName:
    cap = load_capabilities()
    for model_name in ("liriope", "wambugu"):
        crops = cap[model_name].get("crops", [])
        if any(int(c["crop_id"]) == int(crop_id) for c in crops):
            return model_name  # type: ignore
    raise ValueError(f"지원하지 않는 crop_id: {crop_id}")


def get_predict_url(model_name: ModelName) -> str:
    cap = load_capabilities()
    url = cap[model_name].get("predict_url")
    if not url:
        raise ValueError(f"{model_name} predict_url이 비어있습니다.")
    return str(url)
