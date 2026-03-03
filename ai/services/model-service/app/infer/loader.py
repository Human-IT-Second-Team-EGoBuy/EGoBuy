# app/infer/loader.py
import os
from pathlib import Path
from typing import Optional, Tuple, List, Dict, Any

import torch
import timm

from app.infer.labels import CLASSES

# ─────────────────────────────────────────────────────────────
# Defaults
# ─────────────────────────────────────────────────────────────
DEFAULT_WEIGHTS_FILENAME = "best_model_epoch8_NEW.pth"
DEFAULT_WEIGHTS_PATH = f"/weights/{DEFAULT_WEIGHTS_FILENAME}"

_DEVICE: Optional[torch.device] = None
_MODEL: Optional[torch.nn.Module] = None
_ARCH: Optional[str] = None


# ─────────────────────────────────────────────────────────────
# Device
# ─────────────────────────────────────────────────────────────
def get_device() -> torch.device:
    global _DEVICE
    if _DEVICE is not None:
        return _DEVICE

    want = (os.getenv("DEVICE") or "").strip().lower()
    if want == "cuda" and torch.cuda.is_available():
        _DEVICE = torch.device("cuda")
    elif want == "cpu":
        _DEVICE = torch.device("cpu")
    else:
        _DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    return _DEVICE


# ─────────────────────────────────────────────────────────────
# Checkpoint helpers
# ─────────────────────────────────────────────────────────────
def _candidate_weights_paths() -> List[Path]:
    """
    가능한 weights 경로 후보들을 만든다.
    - 1순위: env WEIGHTS_PATH
    - 2순위: 도커 기본 (/app/weights/...)
    - 3순위: 로컬 개발용 (app/weights/...)  너 프로젝트 구조 대응
    """
    candidates: List[Path] = []

    env_path = (os.getenv("WEIGHTS_PATH") or "").strip()
    if env_path:
        candidates.append(Path(env_path))

    candidates.append(Path(DEFAULT_WEIGHTS_PATH))

    # loader.py 위치: .../app/infer/loader.py
    # app_dir = .../app
    app_dir = Path(__file__).resolve().parents[1]
    candidates.append(app_dir / "weights" / DEFAULT_WEIGHTS_FILENAME)

    return candidates


def _resolve_weights_path() -> Path:
    for p in _candidate_weights_paths():
        if p.exists():
            return p
    # 전부 실패면, 가장 중요한 후보들 같이 보여주기
    tried = "\n".join([f"- {p}" for p in _candidate_weights_paths()])
    raise FileNotFoundError(
        "weights not found. Tried:\n"
        f"{tried}\n\n"
        "힌트: 로컬 실행이면 WEIGHTS_PATH를 실제 .pth 경로로 export 하세요.\n"
        '예) export WEIGHTS_PATH="$(pwd)/app/weights/best_model_epoch8_NEW.pth"'
    )


def _load_checkpoint() -> Tuple[str, Dict[str, Any]]:
    p = _resolve_weights_path()
    ckpt = torch.load(p, map_location="cpu")

    # ckpt가 state_dict만 덩그러니 저장된 경우도 있으니 dict로 맞추기
    if not isinstance(ckpt, dict):
        raise ValueError(f"Unexpected checkpoint type: {type(ckpt)} (path={p})")

    arch = ckpt.get("arch", "efficientnet_b2")
    return arch, ckpt


def _extract_state_dict(ckpt: Dict[str, Any]) -> Dict[str, Any]:
    """
    체크포인트 포맷이 다양한 경우를 모두 커버:
    - {"model_state_dict": {...}}
    - {"state_dict": {...}}
    - ckpt 자체가 state_dict인 경우
    """
    state = ckpt.get("model_state_dict")
    if isinstance(state, dict) and state:
        return state

    state = ckpt.get("state_dict")
    if isinstance(state, dict) and state:
        return state

    # ckpt 자체가 state_dict인 케이스
    # (단, arch/classes 같은 메타키가 있으면 ckpt 자체는 state_dict가 아님)
    meta_keys = {"arch", "classes", "epoch", "optimizer_state_dict", "scheduler_state_dict"}
    if not (set(ckpt.keys()) & meta_keys):
        return ckpt

    # 여기까지 왔으면 state_dict가 없는 포맷
    raise ValueError("Checkpoint에 model_state_dict/state_dict가 없습니다.")


def _strip_prefix(sd: Dict[str, Any], prefix: str) -> Dict[str, Any]:
    """
    sd 키가 prefix로 시작하는 게 '대부분'이면 prefix 제거.
    (혼합 케이스에서 일부만 제거하면 오히려 더 꼬일 수 있어서 majority 기준)
    """
    keys = list(sd.keys())
    if not keys:
        return sd

    starts = [k.startswith(prefix) for k in keys]
    ratio = sum(starts) / len(starts)

    # 70% 이상이 prefix로 시작하면 "그게 표준"이라 보고 제거
    if ratio >= 0.7:
        new_sd: Dict[str, Any] = {}
        for k, v in sd.items():
            if k.startswith(prefix):
                new_sd[k[len(prefix):]] = v
            else:
                # 혹시 섞여있으면 그대로 유지
                new_sd[k] = v
        return new_sd

    return sd


def _normalize_state_dict(sd: Dict[str, Any]) -> Dict[str, Any]:
    """
    자주 등장하는 prefix 정리:
    - DataParallel: module.
    - Wrapper: backbone.  ✅ 너 케이스
    - 기타: model., net.
    """
    sd = _strip_prefix(sd, "module.")
    sd = _strip_prefix(sd, "backbone.")
    sd = _strip_prefix(sd, "model.")
    sd = _strip_prefix(sd, "net.")
    return sd


def _validate_classes_order(ckpt: Dict[str, Any]) -> None:
    ckpt_classes = ckpt.get("classes") or []
    if not ckpt_classes:
        raise ValueError("Checkpoint에 classes가 없습니다. (ckpt['classes']가 비어있음)")

    if list(ckpt_classes) != list(CLASSES):
        raise ValueError(
            "Checkpoint classes 순서와 labels.py의 CLASSES 순서가 다릅니다.\n"
            "→ labels.py의 CLASSES를 ckpt['classes']와 동일하게 맞추세요."
        )


# ─────────────────────────────────────────────────────────────
# Public: model loader
# ─────────────────────────────────────────────────────────────
def get_processor() -> torch.nn.Module:
    global _MODEL, _ARCH
    if _MODEL is not None:
        return _MODEL

    device = get_device()
    arch, ckpt = _load_checkpoint()

    # classes 순서 검증 (매우 중요)
    _validate_classes_order(ckpt)

    # state_dict 추출 + prefix 정리
    state = _extract_state_dict(ckpt)
    state = _normalize_state_dict(state)

    # 모델 생성
    model = timm.create_model(arch, pretrained=False, num_classes=len(CLASSES))

    # 로드 (strict=True 유지)
    try:
        model.load_state_dict(state, strict=True)
    except RuntimeError as e:
        # 디버깅에 도움되는 최소 정보만 추가
        sample_keys = list(state.keys())[:5]
        raise RuntimeError(
            f"{e}\n\n"
            f"[debug] arch={arch}, num_classes={len(CLASSES)}\n"
            f"[debug] state_dict sample keys={sample_keys}\n"
            f"[debug] hint: checkpoint가 wrapper(backbone.)/dataparallel(module.)로 저장됐으면 "
            f"prefix 제거가 필요합니다."
        ) from e

    model.to(device)
    model.eval()

    _MODEL = model
    _ARCH = arch
    return _MODEL


def warmup(
    num_iters: int = 1,
    image_size: Tuple[int, int] = (224, 224),
    batch_size: int = 1,
) -> None:
    model = get_processor()
    device = next(model.parameters()).device

    h, w = image_size
    dummy = torch.zeros((batch_size, 3, h, w), device=device)

    with torch.inference_mode():
        for _ in range(max(1, int(num_iters))):
            out = model(dummy)
            if isinstance(out, (tuple, list)) and len(out) > 0:
                _ = out[0]


def get_classes() -> List[str]:
    return CLASSES