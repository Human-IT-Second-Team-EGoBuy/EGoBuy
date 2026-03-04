FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    CHROMA_PERSIST_DIR=/app/data/chroma_db

WORKDIR /app

# 먼저 requirements 복사
COPY requirements-embedder.txt .

# PyPI를 기본 인덱스로 강제 + torch 인덱스는 extra로
RUN pip install --upgrade pip \
 && pip install --no-cache-dir \
    --index-url https://pypi.org/simple \
    --extra-index-url https://download.pytorch.org/whl/cpu \
    -r requirements-embedder.txt

COPY app ./app
COPY data ./data

EXPOSE 8030
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8030"]