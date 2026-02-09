FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    CHROMA_PERSIST_DIR=/app/data/chroma_db

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libglib2.0-0 \
  && rm -rf /var/lib/apt/lists/*

COPY requirements-embedder.txt .
RUN pip install --upgrade pip \
 && pip install --no-cache-dir -r requirements-embedder.txt

COPY app ./app
COPY data ./data

EXPOSE 8030
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8030"]
