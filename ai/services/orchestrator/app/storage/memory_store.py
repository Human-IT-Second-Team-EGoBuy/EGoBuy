import time
import uuid
from typing import Dict, List, Any

_STORE: Dict[str, Dict[str, Any]] = {}

def new_conversation_id() -> str:
    return f"c_{uuid.uuid4().hex[:12]}"

def save_diagnosis(conversation_id: str, diagnosis: dict, evidence: list[dict]) -> None:
    _STORE.setdefault(conversation_id, {})
    _STORE[conversation_id]["diagnosis"] = diagnosis
    _STORE[conversation_id]["evidence"] = evidence
    _STORE[conversation_id].setdefault("messages", [])
    _STORE[conversation_id]["updated_at"] = time.time()

def append_message(conversation_id: str, role: str, content: str) -> None:
    _STORE.setdefault(conversation_id, {})
    _STORE[conversation_id].setdefault("messages", [])
    _STORE[conversation_id]["messages"].append({"role": role, "content": content})
    _STORE[conversation_id]["updated_at"] = time.time()

def get_context(conversation_id: str) -> dict:
    return _STORE.get(conversation_id, {})
