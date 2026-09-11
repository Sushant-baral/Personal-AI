import chromadb

from app.core.config import settings

_client = chromadb.PersistentClient(path=settings.chroma_path)
_collection = _client.get_or_create_collection("memories")


def add_vector(memory_id: int, embedding: list[float], content: str) -> None:
    """Store an embedding in Chroma, linked to its SQLite memory id."""
    _collection.add(
        ids=[str(memory_id)],
        embeddings=[embedding],
        documents=[content],
    )


def search(query_embedding: list[float], top_k: int = 3) -> list[dict]:
    """Return the top_k most similar memories: [{id, content, distance}, ...]."""
    results = _collection.query(query_embeddings=[query_embedding], n_results=top_k)

    matches = []
    ids = results["ids"][0]
    documents = results["documents"][0]
    distances = results["distances"][0]

    for i in range(len(ids)):
        matches.append({
            "id": int(ids[i]),
            "content": documents[i],
            "distance": distances[i],
        })

    return matches
