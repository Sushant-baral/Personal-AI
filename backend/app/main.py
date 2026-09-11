from fastapi import FastAPI

from app.api.routes import router
from app.database.database import init_db

app = FastAPI(title="Jarvis")
app.include_router(router)


@app.on_event("startup")
def startup():
    init_db()


@app.get("/health")
def health():
    return {"status": "online"}
