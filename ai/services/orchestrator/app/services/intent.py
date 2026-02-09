from typing import Literal
from pydantic import BaseModel, Field
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import PydanticOutputParser
import os

OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
_intent_llm = None

class IntentClassification(BaseModel):
    질문의도: Literal["재배법", "병충해", "둘 다", "해당없음"] = Field(description="유저 질문 의도")

def get_intent_llm():
    global _intent_llm
    if _intent_llm is None:
        _intent_llm = ChatOpenAI(model=OPENAI_MODEL, temperature=0)
    return _intent_llm

def classify_intent(question: str) -> str:
    llm = get_intent_llm()
    parser = PydanticOutputParser(pydantic_object=IntentClassification)

    prompt = ChatPromptTemplate.from_messages([
        ("system",
         "너는 농업 질문 분류 전문가야.\n"
         "질문을 읽고 '병충해', '재배법', '둘 다', 또는 '해당없음' 중 하나로만 분류해.\n"
         "{format_instructions}"),
        ("human", "{question}")
    ]).partial(format_instructions=parser.get_format_instructions())

    chain = prompt | llm | parser
    return chain.invoke({"question": question}).질문의도
