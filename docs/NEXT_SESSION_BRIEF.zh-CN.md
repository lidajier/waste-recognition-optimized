# 垃圾识别与智能回收指导系统 - 下次对话交接文档

## 1. 项目当前目标

本项目是一个用于毕业设计展示的完整软件系统，目标是实现：

1. 基于 `YOLO26` 的垃圾识别
2. 图片上传后展示检测框、类别、置信度
3. 将识别结果传给大模型，生成垃圾回收建议
4. 支持多轮追问
5. 支持用户登录、注册、历史图库、错误样本管理、模型实验记录

## 2. 当前技术栈

- 前端：`Vue 3 + Tailwind CSS + Vue Router`
- 后端：`Java 17 + Spring Boot`
- 数据库：`PostgreSQL`
- 推理服务：`Python + FastAPI + Ultralytics`
- 模型权重：`exp.pt`

## 3. 当前已经完成的内容

### 3.1 前端

已完成路由化重构：

- `/login`：登录 / 注册页
- `/app/overview`：系统总览
- `/app/workspace`：识别工作台
- `/app/assistant`：AI 对话页
- `/app/gallery`：历史图库
- `/app/review`：错误样本管理
- `/app/experiments`：模型实验页

已实现功能：

1. 登录后才能进入系统
2. 图片上传与识别
3. 检测框可视化展示
4. 历史图库预览
5. 收藏图片
6. 标记错误样本
7. 删除图片
8. 参数调节：`imgsz/conf/iou`
9. 保存实验记录

### 3.2 后端

已实现接口：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/files/upload`
- `GET /api/files/{imageId}/content`
- `PATCH /api/files/{imageId}`
- `DELETE /api/files/{imageId}`
- `POST /api/detect`
- `POST /api/advice`
- `POST /api/chat/{sessionId}`
- `GET /api/sessions/{sessionId}`
- `GET /api/history`
- `GET /api/gallery`

已实现能力：

1. JWT 登录认证
2. 用户隔离的图片、图库和会话数据
3. 图片状态管理：收藏、错误标记、软删除、备注

### 3.3 推理与模型

已完成：

1. 本地 Python 推理服务可运行
2. 使用 `exp.pt` 推理
3. 固定基线参数：`imgsz=512 conf=0.25 iou=0.7`
4. 提供基线诊断脚本：`inference-service/tools/diagnose_baseline.py`

## 4. 当前运行地址

- 前端：`http://localhost:5173/login`
- 后端：`http://localhost:8080`
- 推理服务：`http://localhost:8001/health`

测试账号：

- 用户名：`demo_user`
- 密码：`demo1234`

数据库信息：

- 数据库：`waste_ai`
- 用户：`postgres`
- 密码：`lsx0630`

## 5. 目前重点注意事项

### 5.1 已修复问题

已修复识别框错位问题：

- 原因是图片缩放与框坐标缩放不同步
- 现在改成图片与框共用同一个预览容器缩放

### 5.2 仍可继续优化的方向

1. 模型实验页可加入图表对比
2. 错误样本页可加入批量操作
3. 图库页可加入搜索、分类、筛选
4. 工作台可升级为实例分割展示
5. 可增加管理员模块和模型版本管理

## 6. 如果下次继续开发，优先建议

建议优先级：

### 方案 A：继续完善毕设系统展示效果

1. 模型实验页增加折线图 / 柱状图
2. 错误样本管理增加批量处理
3. 图库加入搜索和筛选
4. 总览页加入系统架构图和统计图

### 方案 B：继续强化模型研究部分

1. 分析本地与云端效果差异
2. 增加错误样本回流训练
3. 做 `imgsz/conf/iou` 参数实验对比
4. 尝试更大模型或实例分割模型

### 方案 C：开始准备论文

1. 写论文提纲
2. 写第 3 章到第 7 章初稿
3. 整理系统架构图、数据库图、接口文档
4. 整理实验结果表和测试表

## 7. 下次可以直接对我说的话

下次你可以直接复制下面任意一句开始：

### 开发类

- 请先阅读 `waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md` 然后继续完善模型实验页
- 请先阅读 `waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md` 然后继续做错误样本批量管理
- 请先阅读 `waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md` 然后继续优化工作台识别可视化

### 论文类

- 请先阅读 `waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md` 然后帮我写毕业设计论文提纲
- 请先阅读 `waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md` 然后帮我写第 3 章到第 7 章初稿
- 请先阅读 `waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md` 然后帮我整理系统设计文档

## 8. 当前最推荐的下一步

如果目标是毕业设计尽快成型，推荐下次优先做：

1. 论文提纲和正文初稿
2. 模型实验页图表化
3. 错误样本管理增强

## 9. 文档位置

本交接文档路径：

`waste-recycling-app/docs/NEXT_SESSION_BRIEF.zh-CN.md`
