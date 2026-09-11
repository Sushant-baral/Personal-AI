from pydantic import BaseModel


class MemoryCreate(BaseModel):
    content: str
    source: str = "user"


class MemoryResponse(BaseModel):
    id: int
    content: str
    source: str
