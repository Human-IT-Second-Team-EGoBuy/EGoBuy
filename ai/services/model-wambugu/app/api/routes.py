# app/api/routes.py
from fastapi import APIRouter, UploadFile, File, HTTPException, Query
from app.infer.loader import get_model, get_processor, get_device
from app.infer.predict import predict_from_bytes

router = APIRouter()

@router.get("/health")
def health():
    device = get_device()
    return {"ok": True, "service": "model-wambugu", "device": str(device)}

@router.post("/predict")
async def predict(
    image: UploadFile = File(...),
    top_k: int = Query(5, ge=1, le=10),
):
    if image.content_type is None or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="image/* 파일만 허용")

    img_bytes = await image.read()
    if not img_bytes:
        raise HTTPException(status_code=400, detail="빈 파일")

    model = get_model()          
    processor = get_processor()  
    device = get_device()

    return predict_from_bytes(
        model=model,
        processor=processor,
        device=device,
        image_bytes=img_bytes,
        top_k=top_k,
    )
