from fastapi import APIRouter
from app.schemas.query import QueryRequest, QueryResponse
from app.vectorstore.repository import query_evidences

router = APIRouter()

@router.get("/health")
def health():
    return {"status": "ok"}

@router.post("/query", response_model=QueryResponse)
def query(req: QueryRequest):
    evidences = query_evidences(req.query, req.intent, req.k)
    return {"evidences": evidences}
