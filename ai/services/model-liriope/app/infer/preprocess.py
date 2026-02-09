# app/infer/preprocess.py

import numpy as np
from PIL import Image, ImageOps

def center_crop(img: Image.Image, ratio: float) -> Image.Image:
    w, h = img.size
    nw, nh = int(w * ratio), int(h * ratio)
    left = (w - nw) // 2
    top = (h - nh) // 2
    return img.crop((left, top, left + nw, top + nh))

def resize_with_pad(img: Image.Image, size=(380, 380), pad_color=(0, 0, 0)) -> Image.Image:
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

def apply_leaf_mask_simple(img: Image.Image) -> Image.Image:
    arr = np.array(img.convert("RGB"), dtype=np.uint8)
    r = arr[..., 0].astype(np.int16)
    g = arr[..., 1].astype(np.int16)
    b = arr[..., 2].astype(np.int16)

    mask = (g > r + 10) & (g > b + 10) & (g > 60)
    out = arr.copy()
    out[~mask] = 0
    return Image.fromarray(out, mode="RGB")

def prep_v1(img: Image.Image) -> np.ndarray:
    x = np.array(img, dtype=np.float32)
    x = np.expand_dims(x, axis=0)
    return x / 127.5 - 1.0

def prep_v2(img: Image.Image) -> np.ndarray:
    x = np.array(img, dtype=np.float32)
    x = np.expand_dims(x, axis=0)
    return x / 255.0

def prep_v3(img: Image.Image) -> np.ndarray:
    x = np.array(img, dtype=np.float32)
    x = np.expand_dims(x, axis=0)
    return x

def preprocess_pil(
    img: Image.Image,
    image_size=(380, 380),
    prep_mode="v3",
    resize_mode="pad",
    crop_ratio=None,
    use_leaf_mask=False,
) -> np.ndarray:
    # 1) EXIF 회전 보정 + RGB
    img = ImageOps.exif_transpose(img).convert("RGB")

    # 2) (옵션) 배경 약화
    if use_leaf_mask:
        img = apply_leaf_mask_simple(img)

    # 3) (옵션) center crop
    if crop_ratio is not None:
        img = center_crop(img, crop_ratio)

    # 4) resize
    if resize_mode == "stretch":
        img = img.resize(image_size, Image.BILINEAR)
    elif resize_mode == "pad":
        img = resize_with_pad(img, image_size)
    else:
        raise ValueError("resize_mode must be 'stretch' or 'pad'")

    # 5) scale
    if prep_mode == "v1":
        return prep_v1(img)
    if prep_mode == "v2":
        return prep_v2(img)
    if prep_mode == "v3":
        return prep_v3(img)

    raise ValueError("prep_mode must be 'v1' or 'v2' or 'v3'")