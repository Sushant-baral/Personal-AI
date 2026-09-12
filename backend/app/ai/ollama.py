import ollama
from app.core.config import settings

_client = ollama.Client(host=settings.ollama_host)


def chat(message: str, heavy: bool = False) -> str:
    model = settings.ollama_heavy_model if heavy else settings.ollama_model
    try:
        response = _client.chat(
            model=model,
            messages=[{"role": "user", "content": message}],
        )
        return response["message"]["content"]
    except ConnectionError:
        raise RuntimeError("Ollama is not running. Start it with: ollama serve")


def embed(text: str) -> list[float]:
    try:
        response = _client.embeddings(
            model=settings.ollama_embed_model,
            prompt=text,
        )
        return response["embedding"]
    except ConnectionError:
        raise RuntimeError("Ollama is not running. Start it with: ollama serve")
