import os
import tempfile
import time
from pathlib import Path

from fastapi import FastAPI, File, Form, UploadFile
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


MODEL_PATH = Path(os.getenv("MODEL_PATH", "../../exp-2.pt")).resolve()
MODEL = YOLO(str(MODEL_PATH))

app = FastAPI(title="Waste Inference Service", version="0.1.0")


@app.get("/health")
def health():
    return {"status": "ok", "model_path": str(MODEL_PATH)}


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
