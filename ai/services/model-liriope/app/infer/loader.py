import os
import numpy as np
from dotenv import load_dotenv
import tensorflow as tf
from huggingface_hub import hf_hub_download

MODEL_ID = "liriope/PlantDiseaseDetection"
MODEL_FILE = "plant_disease_efficientnetb4.h5"

_MODEL_CACHE: tf.keras.Model | None = None
_MODEL_PATH_CACHE: str | None = None

def get_model() -> tf.keras.Model:
    """
    서버 프로세스에서 1회만 로드하고 이후 재사용한다.
    """
    global _MODEL_CACHE, _MODEL_PATH_CACHE

    if _MODEL_CACHE is not None:
        return _MODEL_CACHE

    load_dotenv()
    token = os.getenv("HUGGINGFACE_HUB_TOKEN")

    model_path = hf_hub_download(
        repo_id=MODEL_ID,
        filename=MODEL_FILE,
        token=token,
    )
    _MODEL_PATH_CACHE = model_path

    model = tf.keras.models.load_model(model_path, compile=False)
    _MODEL_CACHE = model
    return _MODEL_CACHE

def warmup(image_size=(380, 380)) -> None:
    """
    TF는 첫 predict가 느린 경우가 많아서 startup 때 더미 입력으로 예열한다.
    """
    model = get_model()
    dummy = np.zeros((1, image_size[0], image_size[1], 3), dtype=np.float32)
    _ = model.predict(dummy, verbose=0)

def get_model_path() -> str | None:
    return _MODEL_PATH_CACHE
