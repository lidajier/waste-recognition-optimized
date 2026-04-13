from pathlib import Path
import sys


ULTRALYTICS_SRC = Path(r"D:/ultralytics")
if str(ULTRALYTICS_SRC) not in sys.path:
    sys.path.insert(0, str(ULTRALYTICS_SRC))

from ultralytics import YOLO


DATA_CFG = Path(r"D:/ultralytics/yolo_garbage/dataset.yaml")
PROJECT_DIR = Path(r"D:/ultralytics/runs/cbam_experiments")


def main():
    model = YOLO("yolov8s.pt")
    model.train(
        data=str(DATA_CFG),
        epochs=5,
        imgsz=512,
        batch=4,
        project=str(PROJECT_DIR),
        name="yolov8s_baseline_waste_fast",
        workers=0,
        patience=5,
        device="cpu",
        cache=True,
    )


if __name__ == "__main__":
    main()
