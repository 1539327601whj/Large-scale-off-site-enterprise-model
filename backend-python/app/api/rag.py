from fastapi import APIRouter, UploadFile, File, Form
from fastapi.responses import StreamingResponse
from app.models.schemas import ChatRequest, UploadRequest, PromptRequest
from app.services import rag_service, llm_service

router = APIRouter()

# 全局系统提示词存储
_system_prompt = None

@router.post("/rag/stream")
async def rag_stream(req: ChatRequest):
    """RAG + 流式对话接口，供 Java 调用"""
    # 1. 检索知识库
    context = rag_service.search(req.message)

    # 2. 使用传入的提示词或全局提示词
    prompt = req.system_prompt or _system_prompt

    # 3. 流式返回
    async def generate():
        async for token in llm_service.chat_stream(
            message=req.message,
            context=context,
            history=req.history,
            system_prompt=prompt
        ):
            # SSE 格式
            yield f"data: {token}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")


@router.post("/rag/upload")
async def upload_doc(req: UploadRequest):
    """上传文档到知识库"""
    import uuid
    doc_id = str(uuid.uuid4())[:8]
    rag_service.add_document(doc_id, req.title, req.content)
    return {"code": 200, "msg": "上传成功", "data": {"doc_id": doc_id}}


@router.delete("/rag/doc/{doc_id}")
async def delete_doc(doc_id: str):
    """删除知识库文档"""
    rag_service.delete_document(doc_id)
    return {"code": 200, "msg": "删除成功"}


@router.get("/rag/docs")
async def list_docs():
    """获取知识库文档数量"""
    count = rag_service.get_doc_count()
    return {"code": 200, "data": {"count": count}}


@router.post("/rag/prompt")
async def set_prompt(req: PromptRequest):
    """设置系统提示词"""
    global _system_prompt
    _system_prompt = req.system_prompt
    return {"code": 200, "msg": "提示词已更新"}


@router.get("/rag/prompt")
async def get_prompt():
    """获取当前系统提示词"""
    return {"code": 200, "data": {"system_prompt": _system_prompt}}
