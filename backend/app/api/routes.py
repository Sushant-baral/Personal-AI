import logging

from fastapi import APIRouter, HTTPException

from app.database.database import SessionLocal
from app.database.models import Conversation, Message, Memory, Setting
from app.memory.embeddings import get_embedding
from app.memory.rag import answer_with_context
from app.memory.vector_store import add_vector, search
from app.schemas.chat import ChatRequest, ChatResponse
from app.schemas.memory import MemoryCreate, MemoryResponse
from app.schemas.settings import SettingsResponse, SettingsUpdate

router = APIRouter()
logger = logging.getLogger("jarvis")

MAX_HISTORY_MESSAGES = 6
DEFAULT_ASSISTANT_NAME = "Assistant"


def _get_assistant_name(db) -> str:
    setting = db.get(Setting, "assistant_name")
    return setting.value if setting else DEFAULT_ASSISTANT_NAME


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

        assistant_name = _get_assistant_name(db)
        answer = answer_with_context(request.message, history=history, assistant_name=assistant_name)

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


@router.get("/settings", response_model=SettingsResponse)
def get_settings():
    db = SessionLocal()
    try:
        return SettingsResponse(assistant_name=_get_assistant_name(db))
    finally:
        db.close()


@router.post("/settings", response_model=SettingsResponse)
def update_settings(request: SettingsUpdate):
    db = SessionLocal()
    try:
        name = request.assistant_name.strip() or DEFAULT_ASSISTANT_NAME
        setting = db.get(Setting, "assistant_name")
        if setting:
            setting.value = name
        else:
            setting = Setting(key="assistant_name", value=name)
            db.add(setting)
        db.commit()
        return SettingsResponse(assistant_name=name)
    finally:
        db.close()
