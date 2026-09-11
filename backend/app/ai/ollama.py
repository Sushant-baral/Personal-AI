import ollama
from app.core.config import settings

_client = ollama.Client(host=settings.ollama_host)


def chat(message: str, heavy: bool = False) -> str:
    """Send a message to the local LLM and return its text response.

    Uses the fast 3B model by default. Set heavy=True for the larger
    7B model on tasks that need deeper reasoning.
    """
    model = settings.ollama_heavy_model if heavy else settings.ollama_model
    try:
        response = _client.chat(
            model=model,
            messages=[{"role": "user", "content": message}],
        )
        return response["message"]["content"]
    except ConnectionError:
        raise RuntimeError(
            "Ollama is not running. Start it with: ollama serve"
        )


def embed(text: str) -> list[float]:
    """Generate an embedding vector for the given text."""
    try:
        response = _client.embeddings(
            model=settings.ollama_embed_model,
            prompt=text,
        )
        return response["embedding"]
    except ConnectionError:
        raise RuntimeError(
            "Ollama is not running. Start it with: ollama serve"
        )
