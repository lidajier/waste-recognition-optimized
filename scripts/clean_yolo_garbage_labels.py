from __future__ import annotations

import json
import shutil
from pathlib import Path


DATASET_ROOT = Path(r"D:/ultralytics/yolo_garbage")
BACKUP_ROOT = Path(r"D:/ultralytics/yolo_garbage_label_backup_20260412")
REPORT_PATH = Path(r"D:/ultralytics/waste-recycling-app/docs/yolo_garbage_label_clean_report.json")


def clean_file(path: Path) -> dict[str, object] | None:
    original = path.read_text(encoding="utf-8", errors="ignore")
    lines = [line.strip() for line in original.splitlines()]
    cleaned = [line for line in lines if line and not line.startswith("#")]
    new_text = "\n".join(cleaned)

    if new_text == "\n".join(line for line in lines if line):
        return None

    backup_path = BACKUP_ROOT / path.relative_to(DATASET_ROOT)
    backup_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(path, backup_path)
    path.write_text(new_text + ("\n" if new_text else ""), encoding="utf-8")
    return {
        "file": str(path.relative_to(DATASET_ROOT)),
        "backup": str(backup_path),
        "remaining_lines": len(cleaned),
    }


def main() -> None:
    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
    BACKUP_ROOT.mkdir(parents=True, exist_ok=True)
    changes: list[dict[str, object]] = []

    for split in ["train", "val"]:
        label_dir = DATASET_ROOT / "labels" / split
        for path in sorted(label_dir.glob("*.txt")):
            result = clean_file(path)
            if result is not None:
                changes.append(result)

    REPORT_PATH.write_text(json.dumps(changes, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Cleaned {len(changes)} label files.")
    print(f"Backup saved to: {BACKUP_ROOT}")
    print(f"Report saved to: {REPORT_PATH}")


if __name__ == "__main__":
    main()
