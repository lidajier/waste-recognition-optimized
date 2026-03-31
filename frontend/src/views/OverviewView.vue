<script setup>
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();

const modules = [
  { title: "用户中心", copy: "登录、注册、历史记录与权限扩展。" },
  { title: "识别工作台", copy: "独立路由承载上传、识别与可视化。" },
  { title: "AI 回收对话", copy: "基于识别结果生成建议并支持追问。" },
  { title: "历史图库", copy: "统一管理已识别图片与标注。" },
  { title: "错误样本回流", copy: "收集误检/漏检样本，形成再训练清单。" },
  { title: "模型实验", copy: "记录 imgsz/conf/iou 对比与指标变化。" }
];

const panels = [
  { title: "识别总量", value: "2,468", hint: "累计识别图片数" },
  { title: "错误样本", value: "128", hint: "待复查样本" },
  { title: "平均时延", value: "412 ms", hint: "本地推理平均" },
  { title: "今日识别", value: "76", hint: "今日新增记录" }
];

const workflows = [
  "数据采集 → 清洗过滤 → 推理识别",
  "误差样本 → 回流训练 → 模型迭代",
  "实验记录 → 指标对比 → 版本归档",
  "识别结果 → 回收建议 → 多轮追问"
];

const tasks = [
  { title: "更新样本库", detail: "新增真实拍摄样本 120 张" },
  { title: "阈值校准", detail: "纸类/塑料类阈值微调" },
  { title: "实验复现", detail: "对比 imgsz=512/640" }
];
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Overview</div>
      <h3 class="section-title">系统总览</h3>
      <p class="section-copy">总览页面只展示看板信息与面板卡片，识别与操作请前往“识别工作台”路由。</p>

      <div class="mt-6 grid gap-4 md:grid-cols-2">
        <div v-for="item in modules" :key="item.title" class="soft-panel">
          <div class="text-lg font-semibold text-slate-900">{{ item.title }}</div>
          <p class="mt-2 text-sm leading-7 text-slate-600">{{ item.copy }}</p>
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Status</div>
      <h3 class="section-title">当前状态</h3>
      <div class="mt-5 space-y-3 text-sm text-slate-600">
        <div class="soft-row">登录用户：{{ store.currentUser.value?.displayName || '--' }}</div>
        <div class="soft-row">图库图片：{{ store.historySummary.value.total }}</div>
        <div class="soft-row">平均时延：{{ store.historySummary.value.avgLatency }} ms</div>
        <div class="soft-row">当前主类别：{{ store.stats.value.top }}</div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Panels</div>
      <h3 class="section-title">数据面板</h3>
      <div class="mt-5 grid gap-4">
        <div v-for="panel in panels" :key="panel.title" class="soft-panel">
          <div class="text-xs uppercase tracking-[0.2em] text-slate-400">{{ panel.title }}</div>
          <div class="mt-2 text-2xl font-semibold text-slate-900">{{ panel.value }}</div>
          <div class="mt-2 text-xs text-slate-500">{{ panel.hint }}</div>
        </div>
      </div>
    </article>

    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Workflow</div>
      <h3 class="section-title">工作流看板</h3>
      <div class="mt-5 grid gap-4 md:grid-cols-2">
        <div v-for="item in workflows" :key="item" class="soft-panel text-sm leading-7 text-slate-600">
          {{ item }}
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Todo</div>
      <h3 class="section-title">本周任务</h3>
      <div class="mt-5 space-y-3">
        <div v-for="task in tasks" :key="task.title" class="soft-panel">
          <div class="text-sm font-semibold text-slate-900">{{ task.title }}</div>
          <div class="mt-2 text-xs text-slate-500">{{ task.detail }}</div>
        </div>
      </div>
    </article>
  </section>
</template>
