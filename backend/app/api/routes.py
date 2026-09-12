import logging

from fastapi import APIRouter, HTTPException

from app.database.database import SessionLocal
from app.database.models import Conversation, Message, Memory
from app.memory.embeddings import get_embedding
from app.memory.rag import answer_with_context
from app.memory.vector_store import add_vector, search
from app.schemas.chat import ChatRequest, ChatResponse
from app.schemas.memory import MemoryCreate, MemoryResponse

router = APIRouter()
logger = logging.getLogger("jarvis")

MAX_HISTORY_MESSAGES = 6


@router.post("/chat", response_model=ChatResponse)
def chat_endpoint(request: ChatRequest):
    db = SessionLocal()
    try:
        if request.conversation_id is not None:
            conversation = db.get(Conversation, request.conversation_id)
            if conversation is None:
                raise HTTPException(status_code=404, detail="Conversation not found")
        else:
            conversation = Conversation()
            db.add(conversation)
            db.flush()

        recent = (
            db.query(Message)
            .filter(Message.conversation_id == conversation.id)
            .order_by(Message.id.desc())
            .limit(MAX_HISTORY_MESSAGES)
            .all()
        )
        recent.reverse()
        history = [{"role": m.role, "content": m.content} for m in recent]

        db.add(Message(conversation_id=conversation.id, role="user", content=request.message))

        answer = answer_with_context(request.message, history=history)

        db.add(Message(conversation_id=conversation.id, role="assistant", content=answer))
        db.commit()

        return ChatResponse(answer=answer, conversation_id=conversation.id)
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
