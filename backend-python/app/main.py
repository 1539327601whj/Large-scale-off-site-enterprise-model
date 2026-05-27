from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import rag

app = FastAPI(title="Enterprise LLM - Python Backend", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(rag.router, prefix="/api", tags=["rag"])

@app.get("/")
async def root():
    return {"msg": "Python RAG Backend is running"}

@app.get("/health")
async def health():
    return {"status": "ok"}
