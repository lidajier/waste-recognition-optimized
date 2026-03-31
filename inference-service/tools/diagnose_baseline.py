import argparse
import json
from collections import Counter
from datetime import datetime
from pathlib import Path

from ultralytics import YOLO


def parse_args():
    parser = argparse.ArgumentParser(description="Run a fixed YOLO baseline diagnosis on a folder of images.")
    parser.add_argument("--model", default="../../exp.pt", help="Path to YOLO weights")
    parser.add_argument("--source", required=True, help="Image folder to evaluate")
    parser.add_argument("--imgsz", type=int, default=512, help="Inference image size")
    parser.add_argument("--conf", type=float, default=0.25, help="Confidence threshold")
    parser.add_argument("--iou", type=float, default=0.7, help="IoU threshold")
    parser.add_argument("--output-dir", default="../reports", help="Directory to save diagnosis report")
    return parser.parse_args()


def main():
    args = parse_args()
    model_path = Path(args.model).resolve()
    source_dir = Path(args.source).resolve()
    output_dir = Path(args.output_dir).resolve()
    output_dir.mkdir(parents=True, exist_ok=True)

    images = sorted(
        [
            p
            for p in source_dir.iterdir()
            if p.is_file() and p.suffix.lower() in {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
        ]
    )
    if not images:
        raise SystemExit(f"No images found in {source_dir}")

    model = YOLO(str(model_path))
    results = model.predict(
        source=[str(path) for path in images],
        imgsz=args.imgsz,
        conf=args.conf,
        iou=args.iou,
        verbose=False,
    )

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    class_counter = Counter()
    no_detection_count = 0
    top_confidences = []
    per_image = []

    for image_path, result in zip(images, results):
        boxes = result.boxes
        if boxes is None or len(boxes) == 0:
            no_detection_count += 1
            per_image.append(
                {
                    "file": image_path.name,
                    "top_class": None,
                    "top_confidence": None,
                    "detections": [],
                }
            )
            continue

        cls_values = boxes.cls.cpu().numpy().astype(int)
        conf_values = boxes.conf.cpu().numpy()
        xyxy_values = boxes.xyxy.cpu().numpy()

        detections = []
        best_index = max(range(len(conf_values)), key=lambda idx: float(conf_values[idx]))
        best_class = result.names[int(cls_values[best_index])]
        best_confidence = float(conf_values[best_index])

        class_counter[best_class] += 1
        top_confidences.append(best_confidence)

        for idx, cls_id in enumerate(cls_values):
            xyxy = xyxy_values[idx]
            detections.append(
                {
                    "class_name": result.names[int(cls_id)],
                    "confidence": float(conf_values[idx]),
                    "box": [float(xyxy[0]), float(xyxy[1]), float(xyxy[2]), float(xyxy[3])],
                }
            )

        per_image.append(
            {
                "file": image_path.name,
                "top_class": best_class,
                "top_confidence": best_confidence,
                "detections": detections,
            }
        )

    summary = {
        "timestamp": timestamp,
        "model": str(model_path),
        "source": str(source_dir),
        "baseline": {
            "imgsz": args.imgsz,
            "conf": args.conf,
            "iou": args.iou,
        },
        "total_images": len(images),
        "no_detection_ratio": round(no_detection_count / len(images), 4),
        "top1_class_distribution": dict(class_counter.most_common()),
        "average_top_confidence": round(sum(top_confidences) / len(top_confidences), 4) if top_confidences else 0.0,
        "output_dir": str(output_dir),
    }

    report_path = output_dir / f"baseline_diagnosis_{timestamp}.json"
    with report_path.open("w", encoding="utf-8") as file:
        json.dump({"summary": summary, "images": per_image}, file, indent=2, ensure_ascii=False)

    print(json.dumps(summary, indent=2, ensure_ascii=False))
    print(f"Report saved to: {report_path}")


if __name__ == "__main__":
    main()
