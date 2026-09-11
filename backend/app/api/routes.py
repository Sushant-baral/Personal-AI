import logging

from fastapi import APIRouter, HTTPException

from app.ai.ollama import chat
from app.database.database import SessionLocal
from app.database.models import Conversation, Message, Memory
from app.memory.embeddings import get_embedding
from app.memory.vector_store import add_vector, search
from app.schemas.chat import ChatRequest, ChatResponse
from app.schemas.memory import MemoryCreate, MemoryResponse

router = APIRouter()
logger = logging.getLogger("jarvis")


@router.post("/chat", response_model=ChatResponse)
def chat_endpoint(request: ChatRequest):
    db = SessionLocal()
    try:
        # For Phase 1, each request creates its own conversation.
        # We'll add multi-turn conversation continuity in a later step.
        conversation = Conversation()
        db.add(conversation)
        db.flush()  # get conversation.id without committing yet

        db.add(Message(conversation_id=conversation.id, role="user", content=request.message))

        answer = chat(request.message)

        db.add(Message(conversation_id=conversation.id, role="assistant", content=answer))
        db.commit()

        return ChatResponse(answer=answer)
    except RuntimeError as e:
        db.rollback()
        logger.error(f"Chat failed: {e}")
        raise HTTPException(status_code=503, detail=str(e))
    finally:
        db.close()


@router.post("/memory", response_model=MemoryResponse)
def create_memory(request: MemoryCreate):
    db = SessionLocal()
    try:
        memory = Memory(content=request.content, source=request.source)
        db.add(memory)
        db.commit()
        db.refresh(memory)

        embedding = get_embedding(memory.content)
        add_vector(memory.id, embedding, memory.content)

        logger.info(f"Memory stored: id={memory.id}")
        return MemoryResponse(id=memory.id, content=memory.content, source=memory.source)
    except RuntimeError as e:
        db.rollback()
        logger.error(f"Memory storage failed: {e}")
        raise HTTPException(status_code=503, detail=str(e))
    finally:
        db.close()


@router.get("/memory/search")
def search_memory(q: str, top_k: int = 3):
    try:
        query_embedding = get_embedding(q)
        return search(query_embedding, top_k=top_k)
    except RuntimeError as e:
        logger.error(f"Memory search failed: {e}")
        raise HTTPException(status_code=503, detail=str(e))
