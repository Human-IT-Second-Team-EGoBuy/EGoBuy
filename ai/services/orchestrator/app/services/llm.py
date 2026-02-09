import os
from langchain_openai import ChatOpenAI

OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")

def get_llm():
    return ChatOpenAI(model=OPENAI_MODEL, temperature=0)

def build_chat_prompt(user_message: str, diagnosis: dict, evidence_docs: list[dict], history: list[dict]) -> str:
    # evidence는 snippet만 (기존 유지)
    ev_text = "\n".join(
        [f"- ({d.get('source','')}) {d.get('title','')}: {d.get('snippet','')}" for d in evidence_docs[:5]]
    )

    # history는 사용하려면 넣고, 규칙상 줄바꿈이 답변에 나오면 안 되니까
    # "입력"으로는 줄바꿈 있어도 상관 없음 (출력 금지 규칙이므로)
    hist_text = "\n".join([f"{m['role']}: {m['content']}" for m in history[-8:]]) if history else ""

    # ✅ 네가 원하는 규칙으로 프롬프트 교체
    return f"""
당신은 친절하고 전문적인 농업 지식 상담사입니다.
규칙:
- 줄바꿈/목록/번호/불릿(예: 1., -, •) 사용 금지.
- 2~3문장 이내.
- 허용되지 않는 요청은 정중히 거절하고 대안을 제시해.

[진단 결과(있을 때만 참고)]
{diagnosis}

[이전 대화(있을 때만 참고)]
{hist_text}

[관련 정보]
{ev_text}

[사용자 질문]
{user_message}
""".strip()