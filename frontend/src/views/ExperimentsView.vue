<script setup>
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-3">
      <div class="flex items-center justify-between gap-4">
        <div>
          <div class="section-kicker">Experiments</div>
          <h3 class="section-title">模型实验页面</h3>
          <p class="section-copy">用于记录不同 `imgsz/conf/iou` 参数下的实验结果，便于毕业设计展示实验对比过程。</p>
        </div>
        <button class="secondary-btn" @click="store.clearExperimentRuns">清空实验记录</button>
      </div>

      <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="item in store.experimentRuns.value" :key="item.id" class="soft-panel">
          <div class="text-xs uppercase tracking-[0.22em] text-emerald-600">{{ store.formatDate(item.createdAt) }}</div>
          <div class="mt-3 text-lg font-semibold text-slate-900">{{ item.topClass }}</div>
          <div class="mt-2 space-y-2 text-sm text-slate-600">
            <div>imgsz: {{ item.imgsz }}</div>
            <div>conf: {{ item.conf }}</div>
            <div>iou: {{ item.iou }}</div>
            <div>boxCount: {{ item.boxCount }}</div>
            <div>latency: {{ item.latencyMs }} ms</div>
            <div>avg confidence: {{ store.formatPercent(item.avgConfidence) }}</div>
            <div>filter conf: {{ item.minConfidenceFilter }}</div>
            <div>keyword: {{ item.classKeywordFilter || '--' }}</div>
            <div>备注: {{ item.note || '无' }}</div>
          </div>
        </div>
      </div>

      <div v-if="store.experimentRuns.value.length" class="mt-6 overflow-auto rounded-[24px] bg-white p-4 shadow-sm">
        <table class="min-w-full text-left text-sm text-slate-600">
          <thead>
            <tr class="border-b border-slate-100 text-xs uppercase tracking-[0.18em] text-slate-400">
              <th class="px-3 py-3">时间</th>
              <th class="px-3 py-3">主类别</th>
              <th class="px-3 py-3">imgsz</th>
              <th class="px-3 py-3">conf</th>
              <th class="px-3 py-3">iou</th>
              <th class="px-3 py-3">目标数</th>
              <th class="px-3 py-3">平均置信度</th>
              <th class="px-3 py-3">时延</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in store.experimentRuns.value" :key="`${item.id}-row`" class="border-b border-slate-50">
              <td class="px-3 py-3">{{ store.formatDate(item.createdAt) }}</td>
              <td class="px-3 py-3">{{ item.topClass || '--' }}</td>
              <td class="px-3 py-3">{{ item.imgsz }}</td>
              <td class="px-3 py-3">{{ item.conf }}</td>
              <td class="px-3 py-3">{{ item.iou }}</td>
              <td class="px-3 py-3">{{ item.boxCount }}</td>
              <td class="px-3 py-3">{{ store.formatPercent(item.avgConfidence) }}</td>
              <td class="px-3 py-3">{{ item.latencyMs }} ms</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!store.experimentRuns.value.length" class="soft-panel mt-6 text-sm text-slate-500">
        先去识别工作台进行一次识别，然后点击“保存当前实验结果”，这里就会出现实验记录。
      </div>
    </article>
  </section>
</template>
