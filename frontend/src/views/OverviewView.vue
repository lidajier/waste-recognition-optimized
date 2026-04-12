<script setup>
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();

const workflows = [
  "数据采集 → 清洗过滤 → 推理识别",
  "误差样本 → 回流训练 → 模型迭代",
  "实验记录 → 指标对比 → 版本归档",
  "识别结果 → 回收建议 → 多轮追问"
];
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Overview</div>
      <h3 class="section-title">系统总览</h3>
      <p class="section-copy">这里展示系统真实运行统计、类别分布和最近识别动态，便于论文中说明系统整体运行状态。</p>

      <div class="mt-6 grid gap-4 md:grid-cols-2">
        <div class="soft-panel">
          <div class="text-xs uppercase tracking-[0.2em] text-slate-400">累计图片数</div>
          <div class="mt-2 text-3xl font-semibold text-slate-900">{{ store.overviewStats.value.totalImages }}</div>
          <p class="mt-2 text-sm leading-7 text-slate-600">系统中已保存的识别图片数量。</p>
        </div>
        <div class="soft-panel">
          <div class="text-xs uppercase tracking-[0.2em] text-slate-400">累计识别目标</div>
          <div class="mt-2 text-3xl font-semibold text-slate-900">{{ store.overviewStats.value.totalDetectedObjects }}</div>
          <p class="mt-2 text-sm leading-7 text-slate-600">用于体现系统对多实例垃圾目标的处理能力。</p>
        </div>
        <div class="soft-panel">
          <div class="text-xs uppercase tracking-[0.2em] text-slate-400">错误样本数</div>
          <div class="mt-2 text-3xl font-semibold text-slate-900">{{ store.overviewStats.value.flaggedCount }}</div>
          <p class="mt-2 text-sm leading-7 text-slate-600">支持后续误检、漏检分析和样本回流。</p>
        </div>
        <div class="soft-panel">
          <div class="text-xs uppercase tracking-[0.2em] text-slate-400">主导类别</div>
          <div class="mt-2 text-3xl font-semibold text-slate-900">{{ store.overviewStats.value.topClass }}</div>
          <p class="mt-2 text-sm leading-7 text-slate-600">根据历史识别结果统计得到的高频垃圾类别。</p>
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Status</div>
      <h3 class="section-title">当前状态</h3>
      <div class="mt-5 space-y-3 text-sm text-slate-600">
        <div class="soft-row">登录用户：{{ store.currentUser.value?.displayName || '--' }}</div>
        <div class="soft-row">图库图片：{{ store.overviewStats.value.totalImages }}</div>
        <div class="soft-row">平均时延：{{ store.overviewStats.value.avgLatencyMs }} ms</div>
        <div class="soft-row">收藏图片：{{ store.overviewStats.value.favoriteCount }}</div>
        <div class="soft-row">当前工作台主类别：{{ store.stats.value.top }}</div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Panels</div>
      <h3 class="section-title">数据面板</h3>
      <div class="mt-5 grid gap-4">
        <div v-for="item in store.overviewStats.value.topClasses" :key="item.className" class="soft-panel">
          <div class="text-xs uppercase tracking-[0.2em] text-slate-400">类别</div>
          <div class="mt-2 text-2xl font-semibold text-slate-900">{{ item.className }}</div>
          <div class="mt-2 text-xs text-slate-500">累计出现 {{ item.count }} 次</div>
        </div>
        <div v-if="!store.overviewStats.value.topClasses.length" class="soft-panel text-sm text-slate-500">暂无类别统计，请先完成若干次识别。</div>
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
      <div class="section-kicker">Recent</div>
      <h3 class="section-title">最近识别</h3>
      <div class="mt-5 space-y-3">
        <div v-for="item in store.overviewStats.value.recentActivities" :key="item.imageId" class="soft-panel">
          <div class="text-sm font-semibold text-slate-900">{{ item.imageName }}</div>
          <div class="mt-2 text-xs text-slate-500">{{ item.topClass || '未识别' }} · {{ item.latencyMs ?? '--' }} ms</div>
          <div class="mt-2 text-xs text-slate-400">{{ store.formatDate(item.uploadedAt) }}</div>
        </div>
        <div v-if="!store.overviewStats.value.recentActivities.length" class="soft-panel text-sm text-slate-500">暂无历史识别记录。</div>
      </div>
    </article>
  </section>
</template>
