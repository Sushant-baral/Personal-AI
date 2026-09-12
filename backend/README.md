# Jarvis — Personal AI Backend (Phase 1)

A local-first personal AI assistant backend. Runs entirely on your own machine —
no cloud APIs, no external data sharing. This is Phase 1: the AI brain. The
Android app and remote connectivity come in later phases.

## What it does

- Chat with a local LLM (via Ollama), with conversation memory
- Explicitly store "memories" and retrieve them later by meaning, not just
  exact text match (semantic search)
- Answers your questions using relevant stored memories when available, and
  says so honestly when it doesn't have enough information

## Requirements

- Arch Linux (or similar)
- Python 3.12+
- [Ollama](https://ollama.com) installed and running

## Ollama setup

Install Ollama, then pull the models this project uses:

```bash
ollama pull qwen2.5:3b-instruct-q4_K_M
ollama pull qwen2.5:7b-instruct-q4_K_M
ollama pull nomic-embed-text
```

The 3B model is the default (fast, low RAM). The 7B model is available for
heavier tasks by passing `heavy=True` to `chat()` in code.

## Python environment setup

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Configuration

Copy `.env.example` to `.env` and adjust if needed:

```bash
cp .env.example .env
```

## Running the server

```bash
uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

Server runs at `http://127.0.0.1:8000`.

## Testing

```bash
pytest -v
```

## API examples

**Health check**

```bash
curl http://127.0.0.1:8000/health
```

**Chat (new conversation)**

```bash
curl -X POST http://127.0.0.1:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is a mutex?"}'
```

Response includes a `conversation_id` — pass it back on follow-up messages so
the assistant remembers the recent conversation:

```bash
curl -X POST http://127.0.0.1:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Can you give an example?", "conversation_id": 1}'
```

**Store a memory**

```bash
curl -X POST http://127.0.0.1:8000/memory \
  -H "Content-Type: application/json" \
  -d '{"content": "My OS exam is tomorrow", "source": "user"}'
```

**Search memories**

```bash
curl "http://127.0.0.1:8000/memory/search?q=exam"
```

## Project structure

```
backend/
├── app/
│   ├── main.py            FastAPI entry point
│   ├── api/routes.py      HTTP endpoints
│   ├── ai/ollama.py       Ollama chat + embeddings
│   ├── memory/            Embeddings, vector store (Chroma), RAG
│   ├── database/          SQLite models (conversations, messages, memories)
│   ├── core/config.py     Settings loaded from .env
│   └── schemas/           Request/response models
├── data/                  SQLite db + Chroma vector store (gitignored)
├── tests/                 Automated tests
└── requirements.txt
```

## Notes

- Everything runs locally: SQLite for structured data, ChromaDB for vector
  search, Ollama for inference and embeddings. No data leaves your machine.
- Conversation history is capped at the last 6 messages per conversation to
  keep prompts from growing unbounded.
