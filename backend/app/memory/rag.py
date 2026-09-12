from app.ai.ollama import chat
from app.memory.embeddings import get_embedding
from app.memory.vector_store import search

PROMPT_TEMPLATE = """You are a personal assistant having a conversation with the \
user. Use the memories and recent conversation below as context if they are \
relevant. Respond naturally to statements and casual messages. Only say you \
don't have enough information if the user asks a direct question you genuinely \
cannot answer from the context below - don't refuse to acknowledge statements.

Memories:
{memories}

Recent conversation:
{history}

User: {question}
"""


def answer_with_context(question: str, history: list[dict] | None = None, top_k: int = 3) -> str:
    query_embedding = get_embedding(question)
    memories = search(query_embedding, top_k=top_k)

    if memories:
        memories_text = "\n".join(f"- {m['content']}" for m in memories)
    else:
        memories_text = "(no relevant memories found)"

    if history:
        history_text = "\n".join(f"{m['role']}: {m['content']}" for m in history)
    else:
        history_text = "(no prior messages)"

    prompt = PROMPT_TEMPLATE.format(
        memories=memories_text, history=history_text, question=question
    )
    return chat(prompt)
