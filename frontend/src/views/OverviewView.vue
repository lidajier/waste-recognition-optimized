<script setup>
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();

const workflows = [
  "采集 · 识别 · 输出建议",
  "问题样本 · 回流 · 迭代",
  "实验记录 · 对比 · 归档",
  "识别结果 · 建议 · 追问"
];
</script>

<template>
  <section class="page-grid">
    <article class="content-card hero-card lg:col-span-2">
      <div class="section-kicker">Overview</div>
      <h3 class="section-title">系统总览</h3>
      <p class="section-copy">当前运行状态、识别规模和最近结果都集中在这里。</p>

      <div class="hero-grid mt-6">
        <div class="hero-panel hero-panel-strong">
          <div class="hero-label">Pipeline Snapshot</div>
          <div class="hero-value">识别 · 建议 · 对话</div>
          <p class="hero-text">检测结果进入建议模块，再延伸到多轮问答。</p>
        </div>
        <div class="hero-panel">
          <div class="hero-label">Current Focus</div>
          <div class="hero-value">校园场景垃圾分类</div>
          <p class="hero-text">围绕教室、宿舍、食堂等高频投放场景展开。</p>
        </div>
      </div>

      <div class="mt-6 overview-stat-grid">
        <div class="soft-panel overview-stat-card">
          <div class="overview-stat-label">累计图片数</div>
          <div class="overview-stat-value">{{ store.overviewStats.value.totalImages }}</div>
          <p class="overview-stat-copy">图库总量</p>
        </div>
        <div class="soft-panel overview-stat-card">
          <div class="overview-stat-label">累计识别目标</div>
          <div class="overview-stat-value">{{ store.overviewStats.value.totalDetectedObjects }}</div>
          <p class="overview-stat-copy">检测对象总数</p>
        </div>
        <div class="soft-panel overview-stat-card">
          <div class="overview-stat-label">错误样本数</div>
          <div class="overview-stat-value">{{ store.overviewStats.value.flaggedCount }}</div>
          <p class="overview-stat-copy">待复核条目</p>
        </div>
        <div class="soft-panel overview-stat-card">
          <div class="overview-stat-label">主导类别</div>
          <div class="overview-stat-value overview-stat-value-text">{{ store.overviewStats.value.topClass }}</div>
          <p class="overview-stat-copy">高频识别类别</p>
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Status</div>
      <h3 class="section-title">当前状态</h3>
      <div class="mt-5 space-y-3 text-sm text-slate-600">
        <div class="soft-row status-row"><span>登录用户</span><strong>{{ store.currentUser.value?.displayName || '--' }}</strong></div>
        <div class="soft-row status-row"><span>图库图片</span><strong>{{ store.overviewStats.value.totalImages }}</strong></div>
        <div class="soft-row status-row"><span>平均时延</span><strong>{{ store.overviewStats.value.avgLatencyMs }} ms</strong></div>
        <div class="soft-row status-row"><span>收藏图片</span><strong>{{ store.overviewStats.value.favoriteCount }}</strong></div>
        <div class="soft-row status-row"><span>当前主类别</span><strong>{{ store.stats.value.top }}</strong></div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Panels</div>
      <h3 class="section-title">数据面板</h3>
      <div class="mt-5 grid gap-4 overview-panel-grid">
        <div v-for="item in store.overviewStats.value.topClasses" :key="item.className" class="soft-panel overview-panel-card">
          <div class="overview-panel-dot"></div>
          <div class="mt-3 text-2xl font-semibold text-slate-900">{{ item.className }}</div>
          <div class="mt-2 text-xs text-slate-500">{{ item.count }} 次</div>
        </div>
        <div v-if="!store.overviewStats.value.topClasses.length" class="soft-panel text-sm text-slate-500">暂无类别统计，请先完成若干次识别。</div>
      </div>
    </article>

    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Workflow</div>
      <h3 class="section-title">流程概览</h3>
      <div class="mt-5 grid gap-4 md:grid-cols-2">
        <div v-for="item in workflows" :key="item" class="soft-panel workflow-card text-sm leading-7 text-slate-600">
          {{ item }}
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Recent</div>
      <h3 class="section-title">最近识别</h3>
      <div class="mt-5 space-y-3">
        <div v-for="item in store.overviewStats.value.recentActivities" :key="item.imageId" class="soft-panel">
          <div class="recent-activity-name text-sm font-semibold text-slate-900" :title="item.imageName">{{ item.imageName }}</div>
          <div class="mt-2 text-xs text-slate-500">{{ item.topClass || '未识别' }} · {{ item.latencyMs ?? '--' }} ms</div>
          <div class="mt-2 text-xs text-slate-400">{{ store.formatDate(item.uploadedAt) }}</div>
        </div>
        <div v-if="!store.overviewStats.value.recentActivities.length" class="soft-panel text-sm text-slate-500">暂无历史识别记录。</div>
      </div>
    </article>
  </section>
</template>
