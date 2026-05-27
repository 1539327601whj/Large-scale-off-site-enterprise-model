import chromadb
from chromadb.config import Settings
from app.config import CHROMA_PERSIST_DIR, RAG_TOP_K, CHUNK_SIZE, CHUNK_OVERLAP
from app.services.embedding_service import embed_texts

_client = None
_collection = None

def get_collection():
    global _client, _collection
    if _collection is None:
        _client = chromadb.PersistentClient(path=CHROMA_PERSIST_DIR)
        _collection = _client.get_or_create_collection(
            name="knowledge_base",
            metadata={"hnsw:space": "cosine"}
        )
    return _collection

def chunk_text(text: str, chunk_size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> list[str]:
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunks.append(text[start:end])
        start = end - overlap
        if start >= len(text):
            break
    return chunks

def add_document(doc_id: str, title: str, content: str):
    collection = get_collection()
    chunks = chunk_text(content)
    if not chunks:
        return
    embeddings = embed_texts(chunks)
    ids = [f"{doc_id}_chunk_{i}" for i in range(len(chunks))]
    metadatas = [{"doc_id": doc_id, "title": title, "chunk_index": i} for i in range(len(chunks))]
    collection.add(ids=ids, embeddings=embeddings, documents=chunks, metadatas=metadatas)

def search(query: str, top_k: int = RAG_TOP_K) -> str:
    collection = get_collection()
    if collection.count() == 0:
        return ""
    query_embedding = embed_texts([query])
    results = collection.query(query_embeddings=query_embedding, n_results=min(top_k, collection.count()))
    if not results["documents"] or not results["documents"][0]:
        return ""
    return "\n---\n".join(results["documents"][0])

def delete_document(doc_id: str):
    collection = get_collection()
    collection.delete(where={"doc_id": doc_id})

def get_doc_count() -> int:
    collection = get_collection()
    return collection.count()
