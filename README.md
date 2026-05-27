# 企业级私有化大语言模型系统

基于 Vue 3 + Spring Boot 3 + FastAPI 的三端分离架构，实现企业级私有化大模型对话系统。

支持普通用户（AI 对话）和管理员（AI 对话 + 知识库管理 + 提示词配置）两种角色。

## 功能特性

- **用户登录与权限控制** — JWT 鉴权，管理员/普通用户双角色，接口级权限拦截
- **AI 对话（SSE 流式）** — 前端 EventSource 接收，打字机效果逐字显示
- **知识库 RAG** — 文档上传后向量化存入 ChromaDB，对话时语义检索增强回答
- **敏感词过滤** — Java 端对用户输入实时过滤
- **Redis 对话历史** — 多轮对话上下文缓存，支持连续对话
- **管理面板** — 上传企业资料、配置系统提示词

## 技术栈

| 层级 | 选型 |
|------|------|
| 前端 | Vue 3 + Element Plus + Pinia + Axios + EventSource |
| Java 后端 | Spring Boot 3 + Spring Security + MyBatis-Plus + MySQL + Redis |
| Python 后端 | FastAPI + ChromaDB + sentence-transformers + Ollama |
| 大模型 | Ollama（本地部署，支持 Qwen3 / DeepSeek 等）|

## 架构与数据流

```
┌──────────┐      SSE       ┌──────────────┐   RestTemplate   ┌──────────────┐
│  Vue 3   │ ◄────────────► │ Spring Boot  │ ◄──────────────► │   FastAPI    │
│  :5173   │                │    :8080     │                  │    :8000     │
└──────────┘                └──────┬───────┘                  └──────┬───────┘
     │                             │                                 │
  EventSource              JWT 鉴权 + 敏感词                 ChromaDB 向量检索
  打字机渲染                Redis 历史上下文                  Ollama 流式生成
```

## 项目结构

```
├── frontend/                        # Vue 3 前端
│   └── src/
│       ├── views/
│       │   ├── Login.vue            # 登录页
│       │   ├── Chat.vue             # AI 对话（SSE + 打字机效果）
│       │   ├── AdminPanel.vue       # 管理面板
│       │   └── Layout.vue           # 主布局
│       ├── router/index.js          # 路由守卫
│       ├── api/index.js             # Axios 封装
│       └── stores/user.js           # Pinia 状态
├── backend-java/                    # Spring Boot 3 后端
│   └── src/main/java/com/demo/
│       ├── controller/              # Auth / Chat / Knowledge 接口
│       ├── service/                 # 业务逻辑
│       ├── config/                  # Security / Cors / Redis 配置
│       ├── entity/                  # 数据实体
│       ├── mapper/                  # MyBatis 映射
│       └── util/JwtUtil.java        # JWT 工具
├── backend-python/                  # FastAPI 后端
│   └── app/
│       ├── api/rag.py               # 流式对话接口
│       ├── services/
│       │   ├── rag_service.py       # ChromaDB 检索
│       │   ├── llm_service.py       # Ollama 流式调用
│       │   └── embedding_service.py # 文本向量化
│       └── config.py                # 配置
└── sql/init.sql                     # 建库建表脚本
```

## 环境准备

| 软件 | 用途 | 安装方式 |
|------|------|---------|
| JDK 21+ | Java 运行环境 | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| Maven 3.9+ | Java 构建工具 | [Apache Maven](https://maven.apache.org/download.cgi) |
| Node.js 18+ | 前端构建 | [Node.js](https://nodejs.org/) |
| Python 3.10+ | Python 运行环境 | [Python](https://www.python.org/) |
| MySQL 8.0+ | 关系数据库 | 已安装 |
| Redis 7.0+ | 缓存 | 已安装 |
| Ollama | 本地大模型推理 | [Ollama](https://ollama.com/download) |

### Ollama 配置

```bash
# 安装后拉取模型（二选一）
ollama pull qwen3:8b          # Qwen3 8B
ollama pull deepseek-r1:1.5b  # DeepSeek 1.5B

# 确认 Ollama 正在运行
ollama serve
```

模型名称需与 `backend-python/app/config.py` 中 `OLLAMA_MODEL` 一致。

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动三个服务

```bash
# 终端 1：Python 后端（端口 8000）
cd backend-python
py -m pip install -r requirements.txt
py -m uvicorn app.main:app --port 8000

# 终端 2：Java 后端（端口 8080）
cd backend-java
mvn spring-boot:run

# 终端 3：Vue 前端（端口 5173）
cd frontend
npm install
npm run dev
```

### 3. 打开浏览器

访问 http://localhost:5173

## 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | admin | admin123 | AI 对话 + 知识库管理 + 提示词配置 |
| 普通用户 | user | user123 | AI 对话 |

## API 接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 用户登录 | 公开 |
| GET | /api/chat/sse | SSE 对话接口 | 登录用户 |
| POST | /api/admin/knowledge/upload | 上传文档到知识库 | 管理员 |
| GET | /api/admin/knowledge/list | 文档列表 | 管理员 |
| DELETE | /api/admin/knowledge/{id} | 删除文档 | 管理员 |
| POST | /api/admin/prompt | 保存系统提示词 | 管理员 |
| GET | /api/admin/prompt | 获取系统提示词 | 管理员 |

---

Copyright (c) 2026. All rights reserved.
