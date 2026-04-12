<script setup>
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();

async function toggleFavorite(item) {
  await store.toggleFavorite(item.imageId, !item.favorite);
}

async function toggleFlag(item) {
  const note = item.flagged ? "" : window.prompt("请输入错误样本备注（可选）", item.reviewNote || "") ?? item.reviewNote;
  await store.toggleFlagged(item.imageId, !item.flagged, note);
}

async function editMeta(item) {
  const reviewType = window.prompt("请输入错误类型：误检 / 漏检 / 类别混淆 / 低置信度", item.reviewType || "") ?? item.reviewType;
  const correctedClass = window.prompt("请输入人工修正类别（可选）", item.correctedClass || "") ?? item.correctedClass;
  const reviewNote = window.prompt("请输入补充备注（可选）", item.reviewNote || "") ?? item.reviewNote;
  await store.updateReviewMeta(item.imageId, { reviewType, correctedClass, reviewNote, flagged: true });
}
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-3">
      <div class="flex items-center justify-between gap-4">
        <div>
          <div class="section-kicker">Review Center</div>
          <h3 class="section-title">错误样本管理</h3>
          <p class="section-copy">这里集中管理被标记为错误样本的图片，适合论文中展示“误检/漏检回流复盘”。</p>
        </div>
        <div class="soft-panel text-sm text-slate-600">当前错误样本 {{ store.flaggedItems.value.length }} 张</div>
      </div>

      <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="item in store.flaggedItems.value" :key="item.imageId" class="gallery-item overflow-hidden">
          <img :src="item.imageUrl" :alt="item.imageName" class="h-48 w-full object-cover" />
          <div class="p-4">
            <div class="flex items-start justify-between gap-3">
              <div>
                <div class="text-lg font-semibold text-slate-900">{{ item.imageName }}</div>
                <div class="mt-2 text-sm text-slate-500">{{ item.topClass || 'Unknown' }} · {{ item.boxCount }} 个目标</div>
              </div>
              <span class="rounded-full bg-rose-100 px-2 py-1 text-xs text-rose-700">错误样本</span>
            </div>
            <div class="mt-3 rounded-2xl bg-rose-50 px-4 py-3 text-sm text-slate-600">{{ item.reviewNote || '暂无备注' }}</div>
            <div class="mt-3 text-xs text-slate-500">错误类型：{{ item.reviewType || '未标注' }}</div>
            <div class="mt-1 text-xs text-slate-500">人工修正：{{ item.correctedClass || '未填写' }}</div>
            <div class="mt-4 grid gap-3 sm:grid-cols-2">
              <button class="secondary-btn" @click="toggleFavorite(item)">{{ item.favorite ? '取消收藏' : '收藏样本' }}</button>
              <button class="secondary-btn" @click="toggleFlag(item)">取消错误标记</button>
              <button class="secondary-btn sm:col-span-2" @click="editMeta(item)">编辑错误信息</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!store.flaggedItems.value.length" class="soft-panel mt-6 text-sm text-slate-500">
        暂无错误样本。你可以先去历史图库标记问题图片，再回来统一管理。
      </div>
    </article>
  </section>
</template>
