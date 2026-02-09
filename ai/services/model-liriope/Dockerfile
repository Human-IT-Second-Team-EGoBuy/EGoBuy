FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# TF/이미지 계열에서 slim이 가끔 요구하는 최소 라이브러리
# (대부분 문제 없으면 build-essential만 남기거나 전부 삭제해도 됨)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libglib2.0-0 \
  && rm -rf /var/lib/apt/lists/*

COPY requirements-liriope.txt .
RUN pip install --upgrade pip \
 && pip install --no-cache-dir -r requirements-liriope.txt

COPY app ./app

EXPOSE 8010
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8010"]
