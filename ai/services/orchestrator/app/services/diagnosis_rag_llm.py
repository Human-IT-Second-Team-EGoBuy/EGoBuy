import os
from langchain_openai import ChatOpenAI

OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")

_llm = None
def get_llm():
    global _llm
    if _llm is None:
        _llm = ChatOpenAI(model=OPENAI_MODEL, temperature=0)
    return _llm

def build_diagnosis_prompt(label_ko: str, evidence_docs: list[dict]) -> str:
    ev_text = "\n".join(
        [f"- ({d.get('source','')}) {d.get('title','')}: {d.get('snippet','')}" for d in evidence_docs[:5]]
    )

    # 진단 페이지는 “후속질문 없음” + “병충해 설명만” + “짧게”
    return f"""
너는 농업 병해충 상담사다.
아래 '병명'과 '근거 문서' 범위 안에서만 설명한다.
규칙:
- 줄바꿈/목록/번호/불릿 사용 금지.
- 2~3문장 이내로만 답한다.

[병명]
{label_ko}

[근거 문서]
{ev_text}

[요청]
증상 특징, 주요 원인(발생 조건), 예방/방제 핵심만 짧게 알려줘.
""".strip()
