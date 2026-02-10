import os

# app/core/config.py 기준으로 2단계 위가 app/
APP_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # .../app
EMBEDDER_ROOT = os.path.dirname(APP_DIR)                               # .../embedder
DATA_DIR = os.path.join(EMBEDDER_ROOT, "data")

# embedder/data/chroma_db
PERSIST_DIR = os.getenv("CHROMA_PERSIST_DIR", os.path.join(DATA_DIR, "chroma_db"))

COLLECTION_CULTIVATION = os.getenv("COLLECTION_CULTIVATION", "cultivation_kb")
COLLECTION_DISEASE = os.getenv("COLLECTION_DISEASE", "disease_kb")
COLLECTION_DIAGNOSIS = os.getenv("COLLECTION_DIAGNOSIS", "diagnosis_kb")

EMB_DEVICE = os.getenv("EMB_DEVICE", "cpu")
EMB_MODEL_NAME = os.getenv("EMB_MODEL_NAME", "BAAI/bge-m3")
EMB_BATCH_SIZE = int(os.getenv("EMB_BATCH_SIZE", "16"))
