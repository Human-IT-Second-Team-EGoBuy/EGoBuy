from fastapi import FastAPI
from app.api.routes import router
from app.infer.loader import warmup

app = FastAPI()
app.include_router(router)

@app.on_event("startup")
def _startup():
    warmup()
