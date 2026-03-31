# 多目标垃圾识别数据集与 CVAT 自动打标指南

## 1. 当前目标

当前阶段不再继续扩软件功能，重点转向：

1. 寻找适合“多种垃圾同图检测”的数据集
2. 建立统一类别体系
3. 使用自动预标注 + 人工修正的方式高效打标
4. 为后续多类别、多目标垃圾检测训练做准备

## 2. 最推荐的数据集路线

建议采用：`公开数据集 + 自采场景数据 + 预标注修正` 的组合。

### 2.1 第一优先级数据集

#### A. TACO

- 名称：`Trash Annotations in Context`
- 类型：目标检测 / 场景垃圾检测
- 特点：真实场景、多目标、复杂背景、适合垃圾混放检测
- 非常适合作为你的主数据集
- 官方站点：`https://tacodataset.org/`
- GitHub：`https://github.com/pedropro/TACO`

适合原因：

1. 不是单物体白底图，而是真实环境图
2. 同一张图里通常有多个垃圾
3. 比较适合论文里写“复杂场景垃圾检测”

#### B. Roboflow Universe 上的垃圾检测数据集

搜索关键词：

- `waste detection`
- `garbage detection`
- `trash detection`
- `recyclable waste`

推荐原因：

1. 很多项目已经整理成检测格式
2. 部分可以直接导出 YOLO 格式
3. 适合快速补充类别和样本量

入口：`https://universe.roboflow.com/`

#### C. Open Images 作为补充

- 适合作为扩展数据集，补充常见物品类别
- 可用于补 `bottle/can/box/cup/bag` 等日常垃圾相关目标
- 入口：`https://storage.googleapis.com/openimages/web/index.html`

### 2.2 第二优先级数据集

#### D. Kaggle 垃圾检测数据集

搜索词：

- `garbage detection dataset`
- `trash object detection`
- `waste sorting dataset`

适合作为补充，不建议作为唯一主数据集。

#### E. 自采数据集

建议自己拍摄：

1. 宿舍场景
2. 食堂场景
3. 教室场景
4. 垃圾桶旁混放场景
5. 桌面多种垃圾混放场景

建议量：

- 起步：`300 ~ 500` 张
- 较稳：`800 ~ 1500` 张

## 3. 推荐的数据集组合方案

最推荐你这样做：

### 方案 A：毕业设计稳妥方案

1. `TACO` 作为主数据集
2. `Roboflow Universe` 补 2~3 个垃圾检测数据集
3. 自己拍一批真实场景混放图

优点：

- 论文好写
- 数据来源清晰
- 泛化会比只用单一数据集更好

### 方案 B：快速启动方案

1. 先用 `TACO`
2. 再补自采数据

优点：

- 启动快
- 管理简单

## 4. 类别设计建议

不要一开始把类别拆得太碎，建议先做 `10 ~ 15` 类。

推荐首版类别：

1. `plastic_bottle`
2. `plastic_box`
3. `plastic_bag`
4. `paper`
5. `cardboard`
6. `metal_can`
7. `glass_bottle`
8. `battery`
9. `food_waste`
10. `cup`
11. `tissue`
12. `chopsticks`
13. `e_waste`
14. `other_waste`

建议策略：

- 模型层做细分类
- 业务层再映射成：可回收物 / 有害垃圾 / 厨余垃圾 / 其他垃圾

这样更适合系统展示和论文表达。

## 5. 最推荐的自动打标方案

最推荐：`CVAT + YOLO 预标注 + 人工修正`

原因：

1. 你已经有 YOLO 模型基础
2. 预标注后再修正，效率很高
3. CVAT 支持检测、分割、导出 YOLO 格式
4. 后续如果你想做实例分割，也能继续用

## 6. CVAT 怎么使用

## 6.1 安装方式建议

最推荐本地 Docker 部署。

如果你本机有 Docker，可以参考 CVAT 官方安装文档：

- 安装文档：`https://docs.cvat.ai/docs/administration/community/basics/installation/`

常见本地启动方式思路：

1. 下载 CVAT 官方仓库
2. 使用 Docker Compose 启动
3. 浏览器访问本地 CVAT 页面
4. 创建账号后开始建项目与任务

如果你不想本地折腾，也可以用 `CVAT Online`，但本地更适合你的毕设项目数据管理。

## 6.2 在 CVAT 中的推荐使用流程

### 第一步：建立 Project

在 CVAT 中先创建一个项目，例如：

- 项目名：`waste-multi-object-detection`

然后在 Project 中预先定义标签：

- `plastic_bottle`
- `plastic_box`
- `plastic_bag`
- `paper`
- `cardboard`
- `metal_can`
- `glass_bottle`
- `battery`
- `food_waste`
- `cup`
- `tissue`
- `chopsticks`
- `e_waste`
- `other_waste`

### 第二步：创建 Task

把图片导入 Task：

1. 从本地导入原始图片
2. 一个 Task 放一批图，比如 `200 ~ 500` 张
3. 大批量数据不要一次塞太多，避免管理困难

### 第三步：先做自动预标注

CVAT 官方自动标注说明：

- 文档：`https://docs.cvat.ai/docs/annotation/auto-annotation/automatic-annotation/`

官方流程大意：

1. 打开任务
2. 选择 `Actions`
3. 点击 `Automatic annotation`
4. 选择模型
5. 匹配标签
6. 设置阈值
7. 开始自动标注

注意：

- CVAT 自带模型不一定专门适合垃圾类别
- 如果你要真正适配垃圾标签，最好用你自己的 YOLO 预标注结果导入 CVAT，或者使用支持自定义模型的方案

## 6.3 最适合你的实际办法：先用 YOLO 批量预标注，再导入 CVAT 修正

你当前最实用的流程不是完全依赖 CVAT 自带模型，而是：

1. 用你现有的 YOLO 模型批量预测图片
2. 生成 YOLO 标签文件
3. 导入 CVAT
4. 人工修正框和类别
5. 再导出 YOLO 格式用于训练

这才是最适合你当前阶段的高效路线。

## 6.4 具体操作建议

### 方案：YOLO 预标注 -> CVAT 修正

#### 第 1 步：准备原始图片目录

例如：

```text
dataset_raw/
  images/
    train/
    val/
```

#### 第 2 步：用 YOLO 批量跑预测

思路：

1. 对原始图片批量推理
2. 输出每张图对应的 YOLO txt 标签
3. 把低置信度也保留一部分，方便人工修正

#### 第 3 步：整理成 YOLO 数据格式

例如：

```text
dataset_yolo/
  images/
    train/
    val/
  labels/
    train/
    val/
```

#### 第 4 步：导入到 CVAT

CVAT 支持导入 YOLO 格式数据，相关格式文档：

- `https://docs.cvat.ai/docs/dataset_management/formats/format-yolo-ultralytics/`

导入后你就能直接看到预标注结果，然后逐张修正。

#### 第 5 步：人工修正重点

优先修这些：

1. 漏框
2. 错框
3. 类别混淆
4. 多个垃圾贴得很近的情况
5. 小目标
6. 遮挡目标

#### 第 6 步：导出修正后的 YOLO 数据

从 CVAT 导出 `Ultralytics YOLO` 格式，直接用于训练。

## 7. 高效打标建议

### 7.1 标注原则统一

先写一份小规范：

1. 一个物体一个框
2. 遮挡但能辨认就标
3. 太小且看不清的可不标，但规则必须统一
4. 类别命名必须固定，不能同义词乱用

### 7.2 先标一小批建立规范

建议先选 `100 ~ 200` 张建立标准样例。

### 7.3 优先补困难样本

重点收集：

1. 多目标混放
2. 光照复杂
3. 遮挡
4. 背景杂乱
5. 小目标
6. 易混类别

### 7.4 错误样本单独存档

你现在系统里已经有“错误样本管理”的思路，后面可以继续和训练数据结合：

- 误检图
- 漏检图
- 低置信度图

这些图优先回流再训练。

## 8. 建议的数据制作流程

推荐你按这个顺序做：

1. 先定最终类别表
2. 下载 `TACO`
3. 从 `Roboflow Universe` 选 2~3 个检测数据集补充
4. 自己拍一批真实混放垃圾图
5. 用现有 YOLO 模型做预标注
6. 在 CVAT 中修正
7. 导出 YOLO 格式
8. 开始训练和实验记录

## 9. 当前最推荐你下一步直接做什么

建议马上开始：

1. 先确定类别字典
2. 先下载 `TACO`
3. 再从 `Roboflow Universe` 找 2~3 个垃圾检测集
4. 然后用 `CVAT + YOLO 预标注` 开始修标

## 10. 下次可以直接让我做什么

下次你可以直接对我说：

- 请先阅读 `waste-recycling-app/docs/DATASET_AND_LABELING_GUIDE.zh-CN.md` 然后帮我制定类别字典
- 请先阅读 `waste-recycling-app/docs/DATASET_AND_LABELING_GUIDE.zh-CN.md` 然后帮我写 YOLO 数据集 yaml
- 请先阅读 `waste-recycling-app/docs/DATASET_AND_LABELING_GUIDE.zh-CN.md` 然后帮我写自动预标注脚本
- 请先阅读 `waste-recycling-app/docs/DATASET_AND_LABELING_GUIDE.zh-CN.md` 然后帮我写训练命令和实验表
