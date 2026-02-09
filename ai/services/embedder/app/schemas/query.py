from typing import Optional, List, Dict, Any, Literal
from pydantic import BaseModel, Field

Intent = Literal["재배법", "병충해", "진단", "둘 다", "해당없음"]

class QueryRequest(BaseModel):
    query: str = Field(..., description="사용자 질문")
    intent: Intent = Field("둘 다", description="검색할 지식베이스 선택")
    k: int = Field(4, ge=1, le=20)

class Evidence(BaseModel):
    content: str
    metadata: Dict[str, Any] = {}
    score: Optional[float] = None

class QueryResponse(BaseModel):
    evidences: List[Evidence]
