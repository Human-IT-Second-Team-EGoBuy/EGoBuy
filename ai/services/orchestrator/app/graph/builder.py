from langgraph.graph import StateGraph, END

from app.graph.state import AnalyzeState
from app.graph.nodes_analyze import run_selected_model, decide_and_finalize
from app.graph.nodes_chat import chat_flow

_analyze_graph = None
_chat_graph = None


def get_analyze_graph():
    global _analyze_graph
    if _analyze_graph is not None:
        return _analyze_graph

    g = StateGraph(AnalyzeState)
    g.add_node("run_selected_model", run_selected_model)
    g.add_node("decide_and_finalize", decide_and_finalize)

    g.set_entry_point("run_selected_model")
    g.add_edge("run_selected_model", "decide_and_finalize")
    g.add_edge("decide_and_finalize", END)

    _analyze_graph = g.compile()
    return _analyze_graph


def get_chat_graph():
    global _chat_graph
    if _chat_graph is not None:
        return _chat_graph

    _chat_graph = chat_flow()
    return _chat_graph
