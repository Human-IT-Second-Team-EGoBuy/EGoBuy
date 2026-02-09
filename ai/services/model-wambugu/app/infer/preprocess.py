# app/infer/preprocess.py
from PIL import Image, ImageOps
from transformers import ViTImageProcessor
import torch

def preprocess_pil(processor: ViTImageProcessor, img: Image.Image, device: torch.device):
    """
    PIL -> processor -> torch tensor
    """
    img = ImageOps.exif_transpose(img).convert("RGB")
    inputs = processor(images=img, return_tensors="pt")
    # inputs: dict(pixel_values=Tensor)
    inputs = {k: v.to(device) for k, v in inputs.items()}
    return inputs
