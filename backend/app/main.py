from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.routes import router
from app.database.database import init_db


@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield


app = FastAPI(title="Jarvis", lifespan=lifespan)
app.include_router(router)


@app.get("/health")
def health():
    return {"status": "online"}
