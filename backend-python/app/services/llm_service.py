import httpx
import json
from app.config import OLLAMA_BASE_URL, OLLAMA_MODEL

async def chat_stream(message: str, context: str = "", history: list = None, system_prompt: str = None):
    """调用 Ollama 流式生成，逐 token yield"""
    messages = []

    # 系统提示词
    default_system = "你是一个企业级 AI 助手，请根据提供的知识库内容回答用户问题。如果知识库中没有相关信息，请基于你的知识回答，并说明这是通用回答。"
    sys = system_prompt if system_prompt else default_system
    if context:
        sys += f"\n\n以下是知识库检索到的相关内容，请优先参考：\n{context}"
    messages.append({"role": "system", "content": sys})

    # 历史对话
    if history:
        messages.extend(history)

    # 当前用户消息
    messages.append({"role": "user", "content": message})

    async with httpx.AsyncClient(timeout=120.0) as client:
        async with client.stream(
            "POST",
            f"{OLLAMA_BASE_URL}/api/chat",
            json={
                "model": OLLAMA_MODEL,
                "messages": messages,
                "stream": True
            }
        ) as response:
            async for line in response.aiter_lines():
                if not line:
                    continue
                try:
                    data = json.loads(line)
                    if data.get("done"):
                        break
                    content = data.get("message", {}).get("content", "")
                    if content:
                        yield content
                except json.JSONDecodeError:
                    continue
