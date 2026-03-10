# app/infer/preprocess.py
from __future__ import annotations

from PIL import Image, ImageOps

def _to_rgb_safe(img: Image.Image) -> Image.Image:
    # 팔레트/알파 포함 이미지 대응
    if img.mode in ("P", "RGBA", "LA"):
        img = img.convert("RGBA")
        bg = Image.new("RGB", img.size, (255, 255, 255))
        bg.paste(img, mask=img.split()[-1])  # alpha
        return bg
    return img.convert("RGB")


def resize_with_pad(img: Image.Image, size=(224, 224), pad_color=(0, 0, 0)) -> Image.Image:
    target_w, target_h = size
    w, h = img.size
    scale = min(target_w / w, target_h / h)
    nw, nh = int(w * scale), int(h * scale)

    img2 = img.resize((nw, nh), Image.BILINEAR)
    new_img = Image.new("RGB", size, pad_color)
    left = (target_w - nw) // 2
    top = (target_h - nh) // 2
    new_img.paste(img2, (left, top))
    return new_img


def preprocess_pil(
    img: Image.Image,
    image_size=(224, 224),
    resize_mode: str = "stretch",   # "stretch" | "pad"
) -> Image.Image:
    #  1) 폰카 EXIF 회전 보정
    img = ImageOps.exif_transpose(img)

    #  2) RGBA/팔레트 안전 변환
    img = _to_rgb_safe(img)

    #  3) resize
    if resize_mode == "stretch":
        img = img.resize(image_size, Image.BILINEAR)
    elif resize_mode == "pad":
        img = resize_with_pad(img, image_size)
    else:
        raise ValueError("resize_mode must be 'stretch' or 'pad'")

    return img