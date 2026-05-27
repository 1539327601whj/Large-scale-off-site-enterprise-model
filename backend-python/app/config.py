import os

# Ollama 配置
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "www.modelscope.cn/unsloth/Qwen3-8B-GGUF:latest")

# ChromaDB 配置
CHROMA_PERSIST_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "chroma_db")

# Embedding 模型
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2")

# RAG 配置
RAG_TOP_K = 5  # 检索返回的文档数
CHUNK_SIZE = 500  # 文档分块大小
CHUNK_OVERLAP = 50  # 分块重叠
