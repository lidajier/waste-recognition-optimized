# Waste Recycling App

This project is a local waste recognition and recycling guidance system.

It contains three parts:

1. `frontend/`: Vue 3 user interface
2. `backend/`: Spring Boot business API + PostgreSQL
3. `inference-service/`: FastAPI + YOLO inference service

## Default Local Setup

- Frontend: `http://localhost:5183`
- Backend: `http://localhost:8080`
- Inference service: `http://localhost:8001`
- Database: `waste_ai`
- DB user: `postgres`
- DB password: `lsx0630`
- Local LLM base URL: `http://localhost:8317/v1`
- Local LLM model: `gpt-5.4`

## Requirements

1. Java 17+
2. Maven 3.9+
3. Python 3.12
4. Node.js 20+
5. PostgreSQL local service

## Start Order

### 1) Start PostgreSQL

Make sure the local database `waste_ai` exists.

### 2) Start Inference Service

Run in `inference-service/`:

```bat
.venv\Scripts\activate
set MODEL_PATH=..\..\exp.pt
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

If the virtual environment needs to be rebuilt, use the stable CPU setup:

```bat
py -3.12 -m venv .venv
.venv\Scripts\python.exe -m pip install --upgrade pip
.venv\Scripts\python.exe -m pip install torch==2.5.1+cpu torchvision==0.20.1+cpu torchaudio==2.5.1+cpu --index-url https://download.pytorch.org/whl/cpu
.venv\Scripts\python.exe -m pip install -r requirements.txt
```

Health check:

```text
http://localhost:8001/health
```

### 3) Start Spring Boot Backend

Run in `backend/`:

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

### 4) Start Frontend

Run in `frontend/`:

```bat
npm run dev -- --host 0.0.0.0
```

Open:

```text
http://localhost:5183
```

## Main APIs

1. `POST /api/files/upload`
2. `POST /api/detect`
3. `POST /api/advice`
4. `POST /api/chat/{sessionId}`
5. `GET /api/sessions/{sessionId}`
6. `GET /api/history`
7. `GET /api/gallery`

## Notes

1. The backend is configured for a local OpenAI-compatible LLM endpoint.
2. The inference environment has been adjusted to use a stable CPU PyTorch setup.
3. For full demo flow, PostgreSQL, backend, inference service, frontend, and local LLM service must all be running.
