from fastapi import FastAPI
from app.api.routes import router
from app.infer.loader import warmup

app = FastAPI()
app.include_router(router)

@app.on_event("startup")
def on_startup():
    warmup(num_iters=1)   # 서버 시작 시 1번만 로딩 + 워밍업
    print("Model loaded and warmed up.")