# 垃圾识别与智能回收指导系统使用文档

本文档用于说明项目当前可用的本地运行方式。内容已按当前环境重新整理，旧的启动说明已废弃，以本文件为准。

## 1. 项目结构

```text
waste-recycling-app/
  backend/            Spring Boot 后端
  frontend/           Vue 3 前端
  inference-service/  FastAPI 推理服务
  docs/               项目文档
```

## 2. 当前默认配置

- 前端地址：`http://localhost:5183`
- 后端地址：`http://localhost:8080`
- 推理服务地址：`http://localhost:8001`
- 数据库：`waste_ai`
- 数据库用户：`postgres`
- 数据库密码：`lsx0630`
- 本地大模型地址：`http://localhost:8317/v1`
- 本地大模型名称：`gpt-5.4`

## 3. 环境要求

- Java 17+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 本地可用
- Python 3.12 可用

说明：

- 推理服务已调整为 CPU 版 PyTorch 方案，优先保证本地稳定运行
- 如果重新安装推理环境，建议继续使用 CPU 版，不要直接切回不确定的 GPU 版

## 4. 启动顺序

建议严格按以下顺序启动。

### 4.1 启动 PostgreSQL

请确认本地 PostgreSQL 已启动，并且存在数据库 `waste_ai`。

如果数据库尚未创建，可先手动创建：

```sql
CREATE DATABASE waste_ai;
```

### 4.2 启动推理服务

在 `waste-recycling-app/inference-service/` 目录执行：

```bat
.venv\Scripts\activate
set MODEL_PATH=..\..\exp.pt
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

如果虚拟环境丢失或损坏，使用以下方式重建：

```bat
py -3.12 -m venv .venv
.venv\Scripts\python.exe -m pip install --upgrade pip
.venv\Scripts\python.exe -m pip install torch==2.5.1+cpu torchvision==0.20.1+cpu torchaudio==2.5.1+cpu --index-url https://download.pytorch.org/whl/cpu
.venv\Scripts\python.exe -m pip install -r requirements.txt
```

健康检查地址：

```text
http://localhost:8001/health
```

### 4.3 启动后端

在 `waste-recycling-app/backend/` 目录执行：

```bat
set DB_URL=jdbc:postgresql://localhost:5432/waste_ai
set DB_USER=postgres
set DB_PASSWORD=lsx0630
set INFERENCE_BASE_URL=http://localhost:8001
set LLM_BASE_URL=http://localhost:8317/v1
set OPENAI_API_KEY=lsx0630
set LLM_MODEL=gpt-5.4
mvn spring-boot:run
```

后端启动成功后访问：

```text
http://localhost:8080
```

### 4.4 启动前端

在 `waste-recycling-app/frontend/` 目录执行：

```bat
npm run dev -- --host 0.0.0.0
```

浏览器访问：

```text
http://localhost:5183
```

## 5. 功能使用流程

1. 打开前端页面并注册或登录
2. 上传垃圾图片
3. 等待识别结果返回并显示检测框
4. 点击生成回收建议
5. 在 AI 对话页面继续追问
6. 到历史图库查看记录或重新加载旧图片

## 6. 关键接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/files/upload`
- `POST /api/detect`
- `POST /api/advice`
- `POST /api/chat/{sessionId}`
- `GET /api/sessions/{sessionId}`
- `GET /api/history`
- `GET /api/gallery`

## 7. 常见问题

### 7.1 页面能打开但不能识别

通常表示推理服务未启动，或 `MODEL_PATH` 未正确设置。

检查项：

- `http://localhost:8001/health` 是否可访问
- `exp.pt` 是否存在于项目上级目录
- 推理服务是否使用了当前文档中的 CPU 版环境

### 7.2 页面能打开但接口报错

通常表示后端未启动或数据库连接失败。

检查项：

- `http://localhost:8080` 对应端口是否已监听
- PostgreSQL 是否启动
- 数据库密码是否仍为 `lsx0630`

### 7.3 对话功能没有返回预期结果

通常表示本地模型服务未启动，或模型名不匹配。

检查项：

- `http://localhost:8317/v1` 是否可用
- `LLM_MODEL` 是否为 `gpt-5.4`
- 本地模型服务是否兼容 OpenAI `chat/completions` 协议

## 8. 当前运行建议

- 先保证前端、后端、推理服务三者都已启动，再测试完整流程
- 如只看界面效果，启动前端即可
- 如要演示完整毕设流程，必须同时启动 PostgreSQL、本地模型服务、后端、推理服务和前端
