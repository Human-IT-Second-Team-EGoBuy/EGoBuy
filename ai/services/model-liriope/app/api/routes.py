import os
from fastapi import APIRouter, UploadFile, File, HTTPException, Query, Form
from app.infer.loader import get_model, get_model_path
from app.infer.predict import predict_from_bytes

router = APIRouter()

@router.get("/health")
def health():
    return {
        "ok": True,
        "service": "model-liriope",
        "model_path": get_model_path(),
    }

@router.post("/predict")
async def predict(
    image: UploadFile = File(...),

    # crop_id를 Form으로 받기 (curl -F 로 들어오는 값)
    crop_id: int | None = Form(None),

    top_k: int = Query(5, ge=1, le=10),

    # 전처리 옵션
    prep_mode: str = Query("v3", pattern="^(v1|v2|v3)$"),
    resize_mode: str = Query("pad", pattern="^(pad|stretch)$"),
    crop_ratio: float | None = Query(None, ge=0.1, le=1.0),

    # 앙상블 옵션
    use_ensemble: bool = Query(False),
    ensemble_crops: str | None = Query(None, description="예: 'None,0.95,0.90'"),

    # 배경 약화 옵션
    use_leaf_mask: bool = Query(False),
):
    if image.content_type is None or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="image/* 파일만 허용")

    img_bytes = await image.read()
    if not img_bytes:
        raise HTTPException(status_code=400, detail="빈 파일")

    model = get_model()

    crops = None
    if use_ensemble:
        if ensemble_crops:
            tmp = []
            for x in ensemble_crops.split(","):
                x = x.strip()
                if x.lower() == "none":
                    tmp.append(None)
                else:
                    tmp.append(float(x))
            crops = tmp
        else:
            crops = [None, 0.95, 0.90]

    result = predict_from_bytes(
        model=model,
        image_bytes=img_bytes,
        top_k=top_k,
        prep_mode=prep_mode,
        resize_mode=resize_mode,
        crop_ratio=crop_ratio,
        use_ensemble=use_ensemble,
        ensemble_crops=crops,
        use_leaf_mask=use_leaf_mask,
        image_size=(380, 380),
        crop_id=crop_id,
    )
    return result
