FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

RUN python -m pip install --no-cache-dir -U pip setuptools wheel

RUN apt-get update && apt-get install -y --no-install-recommends \
    libjpeg62-turbo zlib1g \
 && rm -rf /var/lib/apt/lists/*

# 1) torch/torchvision (CPU)
RUN pip install --no-cache-dir \
    --index-url https://pypi.org/simple \
    --extra-index-url https://download.pytorch.org/whl/cpu \
    "torch==2.10.0+cpu" "torchvision==0.25.0+cpu"

# 2) 나머지 의존성
COPY requirements-model.txt /app/requirements-model.txt
RUN pip install --no-cache-dir -r /app/requirements-model.txt

COPY app /app/app

#  런타임에 compose 볼륨으로 /weights 주입
ENV WEIGHTS_PATH=/weights/best_model_epoch8_NEW.pth
ENV DEVICE=cpu

EXPOSE 8010
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8010"]