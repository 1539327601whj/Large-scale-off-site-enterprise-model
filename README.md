# 企业级私有化大语言模型系统

## 项目概述

基于 Vue 3 + Spring Boot 3 + FastAPI 的三端分离架构，实现企业级私有化大模型对话系统 Demo。

支持普通用户（基础对话）和管理员（对话 + 上传企业资料 + 配置提示词）两种角色。

## 技术栈

| 层级 | 选型 |
|------|------|
| 前端 | Vue 3 + Element Plus + Axios + EventSource(SSE) |
| Java 后端 | Spring Boot 3 + Spring Security + MyBatis-Plus + MySQL + Redis |
| Python 后端 | FastAPI + ChromaDB + sentence-transformers + Ollama(DeepSeek) |
| 数据库 | MySQL（用户、角色、配置）+ Redis（对话上下文缓存）|
| 向量库 | ChromaDB（本地嵌入式，无需额外部署）|

## 完整数据流

```
用户输入 → Vue(EventSource监听SSE)
  → Java(/api/chat/sse)
    → 鉴权(JWT) → 敏感词过滤 → Redis取历史上下文
    → RestTemplate同步调Python
      → Python(/api/rag/stream)
        → ChromaDB语义检索 → 拼接Prompt → 调Ollama流式生成
        → 每个token通过SSE流式返回Java
    → Java用SseEmitter转发每个token给前端
  → Vue收到message事件 → 实时更新DOM → 打字机效果
```

## 项目结构

```
Privatized enterprise model/
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── views/
│   │   │   ├── Login.vue        # 登录页（区分角色）
│   │   │   ├── Chat.vue         # 对话页（打字机效果）
│   │   │   ├── AdminPanel.vue   # 管理面板（上传资料+提示词）
│   │   │   └── Layout.vue       # 主布局
│   │   ├── router/index.js      # 路由守卫（角色控制）
│   │   ├── api/index.js         # API 封装
│   │   ├── stores/user.js       # Pinia 用户状态
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── backend-java/                # Spring Boot 3 后端
│   ├── src/main/java/com/demo/
│   │   ├── controller/
│   │   │   ├── AuthController.java      # 登录
│   │   │   ├── ChatController.java      # SSE 对话接口
│   │   │   └── KnowledgeController.java # 上传资料+提示词
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── ChatService.java         # SSE核心：调Python+转发
│   │   │   ├── SensitiveWordService.java # 敏感词过滤
│   │   │   └── KnowledgeService.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── RedisConfig.java
│   │   ├── entity/
│   │   ├── mapper/
│   │   └── util/JwtUtil.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── backend-python/              # FastAPI 后端
│   ├── app/
│   │   ├── main.py
│   │   ├── api/rag.py           # 流式接口
│   │   ├── services/
│   │   │   ├── rag_service.py   # ChromaDB检索
│   │   │   ├── llm_service.py   # Ollama流式调用
│   │   │   └── embedding_service.py
│   │   ├── models/schemas.py
│   │   └── config.py
│   ├── requirements.txt
│   └── chroma_db/
├── sql/init.sql
└── README.md
```

## 实施步骤（共 10 步）

| 步骤 | 内容 | 状态 |
|------|------|------|
| 1 | 数据库初始化（建库建表 + 预置数据）| ⬜ |
| 2 | Java 后端 - 项目骨架（pom.xml + 配置类）| ⬜ |
| 3 | Java 后端 - 登录鉴权（JWT + Spring Security）| ⬜ |
| 4 | Python 后端 - RAG 核心（FastAPI + ChromaDB + Ollama）| ⬜ |
| 5 | Java 后端 - SSE 对话核心（SseEmitter + 调Python）| ⬜ |
| 6 | Java 后端 - 管理员接口（上传+提示词）| ⬜ |
| 7 | Vue 前端 - 项目搭建 + 登录页 | ⬜ |
| 8 | Vue 前端 - 对话页面（SSE + 打字机效果）| ⬜ |
| 9 | Vue 前端 - 管理面板 | ⬜ |
| 10 | 联调测试 | ⬜ |

## 需要的软件

| 软件 | 版本 | 状态 |
|------|------|------|
| JDK | 21 ✅ | 已安装 |
| Maven | 3.9 ✅ | 已安装 |
| Node.js | 24 ✅ | 已安装 |
| Python | 3.13 ✅ | 已安装 |
| MySQL | 9.5 ✅ | 已安装 |
| Redis | 8.4 ✅ | 已安装 |
| Ollama | - | 需部署 |

## 启动方式

```bash
# 1. 启动 Python 后端（端口 8000）
cd backend-python
py -m uvicorn app.main:app --port 8000

# 2. 启动 Java 后端（端口 8080）
cd backend-java
mvn spring-boot:run

# 3. 启动 Vue 前端（端口 5173）
cd frontend
npm run dev
```

浏览器打开 http://localhost:5173

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |

---

Copyright (c) 2026. All rights reserved.
