from app.ai.ollama import embed


def get_embedding(text: str) -> list[float]:
    return embed(text)
