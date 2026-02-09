FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# torch/transformers는 보통 wheel로 설치되지만,
# 일부 환경에서 컴파일이 필요한 경우 대비(문제 없으면 삭제 가능)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
  && rm -rf /var/lib/apt/lists/*

COPY requirements-wambugu.txt .
RUN pip install --upgrade pip \
 && pip install --no-cache-dir -r requirements-wambugu.txt

COPY app ./app

EXPOSE 8020
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8020"]
