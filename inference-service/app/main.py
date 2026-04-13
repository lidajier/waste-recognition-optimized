import os
import tempfile
import time
from pathlib import Path
from threading import Lock

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from pydantic import BaseModel
from ultralytics import YOLO


class Detection(BaseModel):
    class_name: str
    confidence: float
    x1: float
    y1: float
    x2: float
    y2: float


class InferResponse(BaseModel):
    model_version: str
    latency_ms: int
    detections: list[Detection]


class ModelInfo(BaseModel):
    model_path: str
    model_version: str


MODEL_LOCK = Lock()
MODEL_PATH = Path(os.getenv("MODEL_PATH", "../../exp-2.pt")).resolve()
MODELS_DIR = Path(os.getenv("MODELS_DIR", "../models")).resolve()
MODELS_DIR.mkdir(parents=True, exist_ok=True)
MODEL = YOLO(str(MODEL_PATH))


app = FastAPI(title="Waste Inference Service", version="0.1.0")


@app.get("/health")
def health():
    return {"status": "ok", "model_path": str(MODEL_PATH)}


@app.get("/model", response_model=ModelInfo)
def current_model():
    return ModelInfo(model_path=str(MODEL_PATH), model_version=MODEL_PATH.name)


@app.post("/model/upload", response_model=ModelInfo)
async def upload_model(file: UploadFile = File(...)):
    suffix = Path(file.filename or "model.pt").suffix.lower()
    if suffix not in {".pt", ".onnx"}:
        raise HTTPException(status_code=400, detail="Only .pt or .onnx model files are supported.")

    target = MODELS_DIR / f"{int(time.time())}_{Path(file.filename or 'model.pt').name}"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(await file.read())
        tmp_path = Path(tmp.name)

    try:
        target.write_bytes(tmp_path.read_bytes())
        switch_model_internal(target)
        return ModelInfo(model_path=str(MODEL_PATH), model_version=MODEL_PATH.name)
    except Exception as e:
        target.unlink(missing_ok=True)
        raise HTTPException(status_code=400, detail=f"Model upload failed: {e}")
    finally:
        tmp_path.unlink(missing_ok=True)


@app.post("/model/use", response_model=ModelInfo)
def use_model(payload: ModelInfo):
    path = Path(payload.model_path).resolve()
    if not path.exists():
        raise HTTPException(status_code=404, detail="Model file not found.")
    switch_model_internal(path)
    return ModelInfo(model_path=str(MODEL_PATH), model_version=MODEL_PATH.name)


def switch_model_internal(path: Path):
    global MODEL_PATH, MODEL
    with MODEL_LOCK:
        model = YOLO(str(path))
        MODEL = model
        MODEL_PATH = path


@app.post("/infer", response_model=InferResponse)
async def infer(
    file: UploadFile = File(...),
    imgsz: int = Form(512),
    conf: float = Form(0.25),
    iou: float = Form(0.7),
):
    suffix = Path(file.filename).suffix if file.filename else ".jpg"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(await file.read())
        tmp_path = Path(tmp.name)

    started = time.time()
    try:
        with MODEL_LOCK:
            results = MODEL.predict(
                source=str(tmp_path),
                imgsz=imgsz,
                conf=conf,
                iou=iou,
                verbose=False,
            )
        elapsed_ms = int((time.time() - started) * 1000)

        detections: list[Detection] = []
        for result in results:
            boxes = result.boxes
            if boxes is None:
                continue

            cls_values = boxes.cls.cpu().numpy().astype(int)
            conf_values = boxes.conf.cpu().numpy()
            xyxy_values = boxes.xyxy.cpu().numpy()

            for idx, cls_id in enumerate(cls_values):
                xyxy = xyxy_values[idx]
                detections.append(
                    Detection(
                        class_name=result.names[int(cls_id)],
                        confidence=float(conf_values[idx]),
                        x1=float(xyxy[0]),
                        y1=float(xyxy[1]),
                        x2=float(xyxy[2]),
                        y2=float(xyxy[3]),
                    )
                )

        return InferResponse(
            model_version=MODEL_PATH.name,
            latency_ms=elapsed_ms,
            detections=detections,
        )
    finally:
        tmp_path.unlink(missing_ok=True)
