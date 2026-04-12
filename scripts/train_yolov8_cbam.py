from pathlib import Path

from ultralytics import YOLO


REPO_ROOT = Path(__file__).resolve().parents[1]
MODEL_CFG = REPO_ROOT / "inference-service" / "models" / "yolov8-cbam.yaml"
DATA_CFG = Path(r"D:/ultralytics/yolo_garbage/dataset.yaml")
PROJECT_DIR = Path(r"D:/ultralytics/runs/cbam_experiments")


def main():
    model = YOLO(str(MODEL_CFG))
    model.train(
        data=str(DATA_CFG),
        epochs=100,
        imgsz=640,
        batch=16,
        project=str(PROJECT_DIR),
        name="yolov8s_cbam_waste",
        pretrained="yolov8s.pt",
        workers=4,
        patience=20,
        device="cpu",
    )


if __name__ == "__main__":
    main()
