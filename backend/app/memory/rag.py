from app.ai.ollama import chat
from app.memory.embeddings import get_embedding
from app.memory.vector_store import search

PROMPT_TEMPLATE = """You are a personal assistant. Your own name is \
{assistant_name} - that is your name, not the user's. If asked your name, \
say {assistant_name}. Never address the user by that name, and never assume \
the user's name unless they have told you it themselves. Never mention any \
underlying model, company, or that you are an AI language model.

Respond naturally and conversationally to statements, casual remarks, \
complaints, small talk, and questions about yourself (how you are, why \
you're slow, what you think, etc.) - these never need "enough information" \
to answer, just respond like a person would. Reserve "I don't have enough \
information" ONLY for questions asking you to recall a specific fact about \
the user's own life that truly isn't in the memories or conversation below \
(e.g. "what's my favorite color" with nothing about it stored).

Use the memories and recent conversation below as context if they are \
relevant.

Memories:
{memories}

Recent conversation:
{history}

User: {question}
"""


def answer_with_context(
    question: str,
    history: list[dict] | None = None,
    top_k: int = 3,
    assistant_name: str = "Assistant",
) -> str:
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
        memories=memories_text,
        history=history_text,
        question=question,
        assistant_name=assistant_name,
    )
    return chat(prompt)
