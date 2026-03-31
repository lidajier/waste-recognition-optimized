CREATE DATABASE waste_ai;
\c waste_ai;

CREATE TABLE IF NOT EXISTS images (
    id UUID PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_path VARCHAR(1024) NOT NULL UNIQUE,
    uploaded_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS detections (
    id UUID PRIMARY KEY,
    image_id UUID NOT NULL REFERENCES images(id),
    model_version VARCHAR(128) NOT NULL,
    imgsz INTEGER NOT NULL,
    conf REAL NOT NULL,
    iou REAL NOT NULL,
    latency_ms BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS detection_items (
    id UUID PRIMARY KEY,
    detection_id UUID NOT NULL REFERENCES detections(id) ON DELETE CASCADE,
    class_name VARCHAR(128) NOT NULL,
    confidence REAL NOT NULL,
    x1 REAL NOT NULL,
    y1 REAL NOT NULL,
    x2 REAL NOT NULL,
    y2 REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS llm_sessions (
    id UUID PRIMARY KEY,
    image_id UUID REFERENCES images(id),
    detection_id UUID REFERENCES detections(id),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS llm_messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES llm_sessions(id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
