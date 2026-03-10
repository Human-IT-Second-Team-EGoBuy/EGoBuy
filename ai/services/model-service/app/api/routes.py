# app/api/routes.py
from fastapi import APIRouter, UploadFile, File, HTTPException, Form
from app.infer.loader import get_processor, get_device
from app.infer.predict import predict_from_bytes

router = APIRouter()

@router.get("/health")
def health():
    device = get_device()
    return {"ok": True, "service": "model-service", "device": str(device)}

@router.post("/predict")
async def predict(
    image: UploadFile = File(...),
    crop_id: int | None = Form(None),
    top_k: int = Form(5),
):
    if image.content_type is None or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="image/* 파일만 허용")

    img_bytes = await image.read()
    if not img_bytes:
        raise HTTPException(status_code=400, detail="빈 파일")
         
    processor = get_processor()  
    device = get_device()

    return predict_from_bytes(
        processor=processor,
        device=device,
        image_bytes=img_bytes,
        top_k=top_k,
        crop_id=crop_id,
    )
