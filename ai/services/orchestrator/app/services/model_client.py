import os
from typing import Any, Dict, Optional
import httpx


# 요청마다 클라이언트 새로 만들면 느려서 전역 재사용
_CLIENT: Optional[httpx.AsyncClient] = None
MODEL_BASE_URL = os.getenv("MODEL_BASE_URL") or "http://model-service:8010/predict"

def get_http_client() -> httpx.AsyncClient:
    global _CLIENT
    if _CLIENT is None:
        _CLIENT = httpx.AsyncClient(timeout=httpx.Timeout(60.0))
    return _CLIENT


async def predict_model(
    image_bytes: bytes,
    image_mime: str,
    top_k: int = 5,
    crop_id: Optional[int] = None,
) -> Dict[str, Any]:
    """
    model-result 의 /predict 엔드포인트 호출 (multipart)
    """

    client = get_http_client()
    mime = image_mime or "image/jpeg"
    files = {
        # 파일명에 확장자 주는 게 안정적
        "image": ("image.jpg", image_bytes, mime ),
    }

    data = {"top_k": str(top_k)}
    if crop_id is not None:
        data["crop_id"] = str(crop_id)

    r = await client.post(MODEL_BASE_URL, files=files, data=data)  
    r.raise_for_status()
    return r.json()
