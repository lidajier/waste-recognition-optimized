from pathlib import Path
import sys


ULTRALYTICS_SRC = Path(r"D:/ultralytics")
if str(ULTRALYTICS_SRC) not in sys.path:
    sys.path.insert(0, str(ULTRALYTICS_SRC))

from ultralytics import YOLO


REPO_ROOT = Path(__file__).resolve().parents[1]
MODEL_CFG = REPO_ROOT / "inference-service" / "models" / "yolov8-cbam.yaml"
DATA_CFG = Path(r"D:/ultralytics/yolo_garbage/dataset.yaml")
PROJECT_DIR = Path(r"D:/ultralytics/runs/cbam_experiments")


def main():
    model = YOLO(str(MODEL_CFG))
    model.train(
        data=str(DATA_CFG),
        epochs=5,
        imgsz=512,
        batch=4,
        project=str(PROJECT_DIR),
        name="yolov8s_cbam_waste_fast",
        pretrained="yolov8s.pt",
        workers=0,
        patience=5,
        device="cpu",
        cache=True,
    )


if __name__ == "__main__":
    main()
