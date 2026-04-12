from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path


DATASET_ROOT = Path(r"D:/ultralytics/yolo_garbage")
BACKUP_ROOT = Path(r"D:/ultralytics/yolo_garbage_dedupe_backup_20260412")
REPORT_PATH = Path(r"D:/ultralytics/waste-recycling-app/docs/yolo_garbage_dedupe_report.json")
IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}


def canonical_label(label_path: Path) -> str:
    if not label_path.exists():
        return ""
    content = label_path.read_text(encoding="utf-8", errors="ignore")
    return "\n".join(line.strip() for line in content.splitlines() if line.strip() and not line.strip().startswith("#"))


def file_sha1(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def move_to_backup(path: Path) -> None:
    backup_path = BACKUP_ROOT / path.relative_to(DATASET_ROOT)
    backup_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(path), str(backup_path))


def main() -> None:
    BACKUP_ROOT.mkdir(parents=True, exist_ok=True)
    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)

    seen: dict[tuple[str, str], tuple[str, Path]] = {}
    removals: list[dict[str, str]] = []

    for split in ["train", "val"]:
        img_dir = DATASET_ROOT / "images" / split
        lbl_dir = DATASET_ROOT / "labels" / split
        for image_path in sorted(img_dir.iterdir()):
            if image_path.suffix.lower() not in IMAGE_EXTS:
                continue

            label_path = lbl_dir / f"{image_path.stem}.txt"
            key = (file_sha1(image_path), canonical_label(label_path))

            if key not in seen:
                seen[key] = (split, image_path)
                continue

            kept_split, kept_image = seen[key]
            remove_image = image_path
            remove_label = label_path

            if kept_split == "val" and split == "train":
                remove_image = kept_image
                remove_label = DATASET_ROOT / "labels" / kept_split / f"{kept_image.stem}.txt"
                seen[key] = (split, image_path)

            move_to_backup(remove_image)
            if remove_label.exists():
                move_to_backup(remove_label)

            removals.append(
                {
                    "removed_image": str(remove_image.relative_to(DATASET_ROOT)),
                    "removed_label": str(remove_label.relative_to(DATASET_ROOT)),
                    "kept_image": str(seen[key][1].relative_to(DATASET_ROOT)),
                    "kept_split": seen[key][0],
                }
            )

    REPORT_PATH.write_text(json.dumps(removals, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Removed {len(removals)} duplicate image-label pairs.")
    print(f"Backup saved to: {BACKUP_ROOT}")
    print(f"Report saved to: {REPORT_PATH}")


if __name__ == "__main__":
    main()
