# YOLOv8-CBAM 轻量改进方案

## 1. 当前结论

- 项目推理服务使用的是 `Ultralytics YOLO`，依赖版本为 `ultralytics==8.4.21`
- 这属于 `YOLOv8` 系列环境，而不是 YOLOv5 或其他框架
- 当前系统默认推理权重为 `exp-2.pt`

## 2. 为什么选择 CBAM

- `CBAM` 已经存在于当前 Ultralytics 代码库中，改动风险小
- 适合写论文中的轻量结构优化
- 能从通道和空间两个维度强化特征表达，适合垃圾检测中的复杂背景和遮挡场景

## 3. 改动位置

本次在 `YOLOv8` 的 `Neck` 多尺度特征融合输出后加入 3 个 `CBAM` 模块：

- 小目标检测分支前 1 个
- 中目标检测分支前 1 个
- 大目标检测分支前 1 个

模型配置文件位于：

- `inference-service/models/yolov8-cbam.yaml`

## 4. 训练数据集建议

当前最适合用于对比实验的数据集为：

- `D:\ultralytics\yolo_garbage`

该数据集为 6 类检测数据集：

- `cardboard`
- `glass`
- `metal`
- `paper`
- `plastic`
- `trash`

## 5. 数据审计结果

- 图片未发现损坏文件
- 标签文件中包含注释行，属于可解释格式，不应直接判为错误
- `yolo_garbage` 中存在大量重复的图像-标签对，需先做去重再训练
- 已提供安全去重脚本：`scripts/dedupe_yolo_garbage.py`
- 已提供标签清洗脚本：`scripts/clean_yolo_garbage_labels.py`

## 6. 数据清洗建议

建议优先执行以下清洗，而不是盲删图片：

- 删除重复的图像-标签对
- 保留 `train`，优先移除与 `train` 重复的 `val` 样本，避免数据泄漏
- 保留标签内容，忽略注释行差异

执行方式：

```bat
python scripts/dedupe_yolo_garbage.py
python scripts/clean_yolo_garbage_labels.py
```

执行后会生成：

- 备份目录：`D:\ultralytics\yolo_garbage_dedupe_backup_20260412`
- 去重报告：`docs/yolo_garbage_dedupe_report.json`
- 标签备份目录：`D:\ultralytics\yolo_garbage_label_backup_20260412`
- 标签清洗报告：`docs/yolo_garbage_label_clean_report.json`

## 7. 训练入口

训练脚本位于：

- `scripts/train_yolov8_cbam.py`

示例运行方式：

```bat
python scripts/train_yolov8_cbam.py
```

## 8. 论文建议表述

可在论文中写为：

“为增强模型在垃圾复杂背景和局部遮挡场景下的特征提取能力，本文在 YOLOv8 的特征融合网络中引入 CBAM 注意力模块，对多尺度融合后的特征进行通道和空间重标定，从而提升模型对关键目标区域的关注能力。”
