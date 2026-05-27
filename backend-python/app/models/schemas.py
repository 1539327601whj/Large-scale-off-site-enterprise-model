from pydantic import BaseModel
from typing import Optional, List

class ChatRequest(BaseModel):
    message: str
    history: Optional[List[dict]] = []  # [{"role": "user/assistant", "content": "..."}]
    system_prompt: Optional[str] = None

class UploadRequest(BaseModel):
    title: str
    content: str

class PromptRequest(BaseModel):
    system_prompt: str
