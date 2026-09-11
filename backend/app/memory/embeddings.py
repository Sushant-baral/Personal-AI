from app.ai.ollama import embed


def get_embedding(text: str) -> list[float]:
    """Generate an embedding vector for the given text."""
    return embed(text)
