FROM python:3.10-slim

# 기본 환경 설정
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
  && rm -rf /var/lib/apt/lists/*

# 의존성 설치
COPY requirements-orchestrator.txt .
RUN pip install --upgrade pip \
 && pip install --no-cache-dir -r requirements-orchestrator.txt


# 앱 코드 복사
COPY app ./app


EXPOSE 8000

# 실행
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]