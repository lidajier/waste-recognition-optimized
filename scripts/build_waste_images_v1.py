import csv
import hashlib
import json
import random
import shutil
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path

try:
    from PIL import Image
except ImportError:  # pragma: no cover
    Image = None


REPO_ROOT = Path(__file__).resolve().parents[1]
WORKSPACE_ROOT = REPO_ROOT.parent
TMP_ROOT = WORKSPACE_ROOT / "tmp_datasets"
OUTPUT_ROOT = REPO_ROOT / "datasets" / "waste_images_v1"
IMAGES_ROOT = OUTPUT_ROOT / "images"
META_ROOT = OUTPUT_ROOT / "meta"

TARGET_PER_CLASS = 25
MIN_SHORT_SIDE = 120
RNG = random.Random(42)
HTTP_HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36"
}


@dataclass(frozen=True)
class SourceSpec:
    source_dataset: str
    source_root: Path
    pattern: str = "*"
    recursive: bool = False
    source_split_or_collection: str = "default"
    original_label: str = ""


CLASS_SPECS = {
    "plastic_bottle": [
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_BottleWhite", original_label="Plastic_BottleWhite"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Bottle_Blue", original_label="Plastic_Bottle_Blue"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Bottle_White", original_label="Plastic_Bottle_White"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_BottleGreen", original_label="Plastic_BottleGreen"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_WaterBottle", original_label="Plastic_WaterBottle"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "plastic" / "plastic bottles", original_label="plastic bottles"),
    ],
    "plastic_box": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "plastic" / "plastic containers", original_label="plastic containers"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Dish", original_label="Plastic_Dish"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Dish_Green", original_label="Plastic_Dish_Green"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Dish_Red", original_label="Plastic_Dish_Red"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Metal_tray", original_label="Plastic_Metal_tray"),
    ],
    "plastic_bag": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "plastic" / "plastic bags", original_label="plastic bags"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_PlasticBag", original_label="Plastic_PlasticBag"),
    ],
    "paper": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "paper" / "paper", original_label="paper"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "paper" / "news paper", original_label="news paper"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_A4Paper", original_label="Paper_A4Paper"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Flyers", original_label="Paper_Flyers"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Magazine", original_label="Paper_Magazine"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_MagazineInchiesta", original_label="Paper_MagazineInchiesta"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_marketMagazine", original_label="Paper_marketMagazine"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Newspaper", original_label="Paper_Newspaper"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Random", original_label="Paper_Random"),
    ],
    "cardboard": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "cardboard", recursive=True, original_label="cardboard"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_CardboardPieces", original_label="Paper_CardboardPieces"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_cargoBox", original_label="Paper_cargoBox"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Box", original_label="Paper_Box"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_CandyBox", original_label="Paper_CandyBox"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Chewingum_Box", original_label="Paper_Chewingum_Box"),
    ],
    "metal_can": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "metal" / "beverage cans", original_label="beverage cans"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Aluminium_Coke", original_label="Aluminium_Coke"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Aluminium_Coke_Zero", original_label="Aluminium_Coke_Zero"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Aluminium_Estathe", original_label="Aluminium_Estathe"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Aluminium_Fanta", original_label="Aluminium_Fanta"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Aluminium_Redbull", original_label="Aluminium_Redbull"),
    ],
    "glass_bottle": [
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_Aperol_bottle_175cl", original_label="Glass_Aperol_bottle_175cl"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_Becks", original_label="Glass_Becks"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_Coke", original_label="Glass_Coke"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_Heineken_bottle_40cl", original_label="Glass_Heineken_bottle_40cl"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_RedBeer", original_label="Glass_RedBeer"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_RedBeer2", original_label="Glass_RedBeer2"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Glass_WaterBottle_SanBenedetto_500ml", original_label="Glass_WaterBottle_SanBenedetto_500ml"),
    ],
    "food_waste": [
        SourceSpec("RealWaste", TMP_ROOT / "realwaste" / "RealWaste" / "Food Organics", original_label="Food Organics"),
    ],
    "cup": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "plastic" / "plastic cups", original_label="plastic cups"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "paper" / "paper cups", original_label="paper cups"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_Cup", original_label="Paper_Cup"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Cup_transparent", original_label="Plastic_Cup_transparent"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Glass", original_label="Plastic_Glass"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Glass_Green", original_label="Plastic_Glass_Green"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Glass_Red", original_label="Plastic_Glass_Red"),
    ],
    "tissue": [
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Paper_NapkinOrPaperTowel", original_label="Paper_NapkinOrPaperTowel"),
    ],
    "chopsticks": [
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Plastic_Cutlery", original_label="Plastic_Cutlery"),
    ],
    "e_waste": [
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "e-waste" / "electrical cables", original_label="electrical cables"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "e-waste" / "electronic chips", original_label="electronic chips"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "e-waste" / "laptops", original_label="laptops"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "e-waste" / "small appliances", original_label="small appliances"),
        SourceSpec("TrashBox", TMP_ROOT / "TrashBox" / "TrashBox_train_dataset_subfolders" / "e-waste" / "smartphones", original_label="smartphones"),
    ],
    "other_waste": [
        SourceSpec("RealWaste", TMP_ROOT / "realwaste" / "RealWaste" / "Miscellaneous Trash", original_label="Miscellaneous Trash"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_BakingPaper", original_label="Unsorted_BakingPaper"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_Bic", original_label="Unsorted_Bic"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_CD", original_label="Unsorted_CD"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_Cigarettes", original_label="Unsorted_Cigarettes"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_lighter", original_label="Unsorted_lighter"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_lighter_blue", original_label="Unsorted_lighter_blue"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_Marker", original_label="Unsorted_Marker"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_Pen", original_label="Unsorted_Pen"),
        SourceSpec("smart-waste-bin", TMP_ROOT / "smart-waste-bin" / "pics" / "Unsorted_Receipt", original_label="Unsorted_Receipt"),
    ],
}

COMMONS_SPECS = {
    "battery": [
        {"category": "Category:AA batteries", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "AA batteries"},
        {"category": "Category:AAA batteries", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "AAA batteries"},
        {"category": "Category:Battery packs", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Battery packs"},
        {"category": "Category:Automobile batteries", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Automobile batteries"},
        {"category": "Category:Lithium-ion batteries", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Lithium-ion batteries"},
        {"category": "Category:Rechargeable batteries", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Rechargeable batteries"},
    ],
    "chopsticks": [
        {"category": "Category:Chopsticks", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Chopsticks"},
        {"category": "Category:Disposable chopsticks", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Disposable chopsticks"},
        {"category": "Category:Wooden chopsticks", "source_dataset": "Wikimedia Commons", "source_split_or_collection": "Wooden chopsticks"},
    ],
}


def ensure_dirs() -> None:
    if OUTPUT_ROOT.exists():
        shutil.rmtree(OUTPUT_ROOT)
    IMAGES_ROOT.mkdir(parents=True, exist_ok=True)
    META_ROOT.mkdir(parents=True, exist_ok=True)
    for class_name in sorted(set(CLASS_SPECS) | set(COMMONS_SPECS)):
        (IMAGES_ROOT / class_name).mkdir(parents=True, exist_ok=True)


def iter_image_files(root: Path, recursive: bool) -> list[Path]:
    if not root.exists():
        return []
    iterator = root.rglob("*") if recursive else root.iterdir()
    files = []
    for path in iterator:
        if path.is_file() and path.suffix.lower() in {".jpg", ".jpeg", ".png", ".bmp", ".webp"}:
            files.append(path)
    return sorted(files)


def image_ok(path: Path) -> bool:
    if Image is None:
        return path.stat().st_size > 0
    try:
        with Image.open(path) as image:
            width, height = image.size
            return min(width, height) >= MIN_SHORT_SIDE
    except Exception:
        return False


def sha1_of_file(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def copy_sources(class_name: str, rows: list[dict], digests: set[str], existing_count: int = 0) -> int:
    class_dir = IMAGES_ROOT / class_name
    count = existing_count
    seen_names = {item.name for item in class_dir.iterdir() if item.is_file()}
    for spec in CLASS_SPECS.get(class_name, []):
        candidates = iter_image_files(spec.source_root, spec.recursive)
        RNG.shuffle(candidates)
        for src in candidates:
            if count >= TARGET_PER_CLASS:
                return count
            if not image_ok(src):
                continue
            file_hash = sha1_of_file(src)
            if file_hash in digests:
                continue
            ext = src.suffix.lower() or ".jpg"
            dest_name = f"{class_name}_{spec.source_dataset.lower().replace(' ', '_')}_{count + 1:03d}{ext}"
            while dest_name in seen_names:
                count += 1
                dest_name = f"{class_name}_{spec.source_dataset.lower().replace(' ', '_')}_{count + 1:03d}{ext}"
            dest = class_dir / dest_name
            shutil.copy2(src, dest)
            digests.add(file_hash)
            seen_names.add(dest_name)
            rows.append(
                {
                    "file_name": dest.name,
                    "final_class": class_name,
                    "source_dataset": spec.source_dataset,
                    "source_split_or_collection": spec.source_split_or_collection,
                    "original_label": spec.original_label,
                    "source_url_or_ref": str(src),
                    "notes": "copied from local public dataset repo" if class_name != "chopsticks" else "proxy sample from cutlery-like public dataset",
                }
            )
            count += 1
    return count


def commons_item_ok(class_name: str, item: dict) -> bool:
    title = item.get("title", "").lower()
    url = item.get("url", "").lower()
    ext = Path(urllib.parse.urlparse(url).path).suffix.lower()
    if ext not in {".jpg", ".jpeg", ".png", ".webp", ".tif", ".tiff"}:
        return False
    if class_name == "battery":
        if not any(token in title for token in ["battery", "batteries", "akku", "cell"]):
            return False
        if any(token in title for token in ["structure", "graph", "chart", "prices", "scheme", "diagram", "charger", "charging", "comparison", "system"]):
            return False
    if class_name == "chopsticks":
        if "chopstick" not in title:
            return False
    return True


def commons_category_files(category: str) -> list[dict]:
    files = []
    cont = None
    while True:
        params = {
            "action": "query",
            "generator": "categorymembers",
            "gcmtitle": category,
            "gcmtype": "file",
            "gcmlimit": "50",
            "prop": "imageinfo",
            "iiprop": "url",
            "format": "json",
        }
        if cont:
            params["gcmcontinue"] = cont
        url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
        request = urllib.request.Request(url, headers=HTTP_HEADERS)
        with urllib.request.urlopen(request, timeout=60) as response:
            payload = json.loads(response.read().decode("utf-8"))
        pages = payload.get("query", {}).get("pages", {})
        for page in pages.values():
            image_info = page.get("imageinfo") or []
            if not image_info:
                continue
            files.append(
                {
                    "title": page.get("title", ""),
                    "url": image_info[0].get("url", ""),
                    "description_url": image_info[0].get("descriptionurl", ""),
                }
            )
        cont = payload.get("continue", {}).get("gcmcontinue")
        if not cont or len(files) >= 120:
            return files


def download_commons(class_name: str, rows: list[dict], digests: set[str], existing_count: int) -> int:
    class_dir = IMAGES_ROOT / class_name
    count = existing_count
    candidates = []
    for spec in COMMONS_SPECS.get(class_name, []):
        for item in commons_category_files(spec["category"]):
            item = item | spec
            if commons_item_ok(class_name, item):
                candidates.append(item)
    RNG.shuffle(candidates)
    for item in candidates:
        if count >= TARGET_PER_CLASS:
            break
        url = item["url"]
        if not url:
            continue
        ext = Path(urllib.parse.urlparse(url).path).suffix.lower() or ".jpg"
        dest = class_dir / f"{class_name}_commons_{count + 1:03d}{ext}"
        try:
            request = urllib.request.Request(url, headers=HTTP_HEADERS)
            with urllib.request.urlopen(request, timeout=120) as response, dest.open("wb") as handle:
                shutil.copyfileobj(response, handle)
        except Exception:
            if dest.exists():
                dest.unlink()
            continue
        if not image_ok(dest):
            dest.unlink(missing_ok=True)
            continue
        file_hash = sha1_of_file(dest)
        if file_hash in digests:
            dest.unlink(missing_ok=True)
            continue
        digests.add(file_hash)
        rows.append(
            {
                "file_name": dest.name,
                "final_class": class_name,
                "source_dataset": item["source_dataset"],
                "source_split_or_collection": item["source_split_or_collection"],
                "original_label": item["title"],
                "source_url_or_ref": item["description_url"] or url,
                "notes": "downloaded from Wikimedia Commons category API",
            }
        )
        count += 1
    return count


def write_csv(path: Path, rows: list[dict], fieldnames: list[str]) -> None:
    with path.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def write_readme(class_counts: dict[str, int], source_counts: dict[str, int]) -> None:
    text = "# waste_images_v1\n\n"
    text += "这是为 `waste-recycling-app` 整理的首版垃圾图片素材集，按 14 个细分类组织，每类目标约 25 张。\n\n"
    text += "## 类别数量\n\n"
    for class_name in sorted(class_counts):
        text += f"- `{class_name}`: {class_counts[class_name]} 张\n"
    text += "\n## 数据来源\n\n"
    for source_name, count in sorted(source_counts.items()):
        text += f"- `{source_name}`: {count} 张\n"
    text += "\n## 筛选原则\n\n"
    text += "- 优先使用公开垃圾数据仓库中的原始图片。\n"
    text += "- 对 `battery` 与 `chopsticks` 使用 Wikimedia Commons 公开分类页补齐。\n"
    text += "- 同类图片控制在约 25 张，避免单一场景过多。\n"
    text += "- 通过文件哈希做基础去重，损坏图与极小图自动跳过。\n"
    text += "\n## 说明\n\n"
    text += "- 本目录当前交付的是图片素材集与来源清单，不包含统一标注文件。\n"
    text += "- 详细来源见 `meta/sources.csv`，汇总见 `meta/class_summary.csv`。\n"
    (OUTPUT_ROOT / "README.zh-CN.md").write_text(text, encoding="utf-8")


def main() -> None:
    ensure_dirs()
    rows = []
    class_summary = []
    digests = set()
    all_classes = sorted(set(CLASS_SPECS) | set(COMMONS_SPECS))
    for class_name in all_classes:
        count = 0
        if class_name in COMMONS_SPECS:
            count = download_commons(class_name, rows, digests, count)
        if count < TARGET_PER_CLASS and class_name in CLASS_SPECS:
            count = copy_sources(class_name, rows, digests, count)
        class_summary.append(
            {
                "final_class": class_name,
                "target_count": TARGET_PER_CLASS,
                "actual_count": count,
                "primary_sources": ", ".join(sorted({row["source_dataset"] for row in rows if row["final_class"] == class_name})),
                "remarks": "ok" if count >= TARGET_PER_CLASS else "needs more images",
            }
        )

    source_counts = {}
    for row in rows:
        source_counts[row["source_dataset"]] = source_counts.get(row["source_dataset"], 0) + 1
    class_counts = {row["final_class"]: 0 for row in rows}
    for row in rows:
        class_counts[row["final_class"]] += 1

    write_csv(
        META_ROOT / "sources.csv",
        rows,
        [
            "file_name",
            "final_class",
            "source_dataset",
            "source_split_or_collection",
            "original_label",
            "source_url_or_ref",
            "notes",
        ],
    )
    write_csv(
        META_ROOT / "class_summary.csv",
        class_summary,
        ["final_class", "target_count", "actual_count", "primary_sources", "remarks"],
    )
    write_readme(class_counts, source_counts)
    print(json.dumps({"output_root": str(OUTPUT_ROOT), "class_counts": class_counts, "source_counts": source_counts}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
