<script setup>
import { useRouter } from "vue-router";
import { useAppStore } from "../composables/useAppStore";

const router = useRouter();
const store = useAppStore();

async function reopen(item) {
  await store.reopenFromGallery(item);
  router.push("/app/workspace");
}

async function reopenChat(item) {
  await store.reopenFromGallery(item);
  router.push("/app/assistant");
}

async function toggleFavorite(item) {
  await store.toggleFavorite(item.imageId, !item.favorite);
}

async function toggleFlag(item) {
  const note = item.flagged ? "" : window.prompt("请输入错误样本说明（可选）", item.reviewNote || "") ?? item.reviewNote;
  await store.toggleFlagged(item.imageId, !item.flagged, note);
}

async function editMeta(item) {
  const reviewType = window.prompt("请输入错误类型：误检 / 漏检 / 类别混淆 / 低置信度", item.reviewType || "") ?? item.reviewType;
  const correctedClass = window.prompt("请输入人工修正类别（可选）", item.correctedClass || "") ?? item.correctedClass;
  const reviewNote = window.prompt("请输入备注（可选）", item.reviewNote || "") ?? item.reviewNote;
  await store.updateReviewMeta(item.imageId, { reviewType, correctedClass, reviewNote, flagged: true });
}

async function removeItem(item) {
  if (!window.confirm(`确认删除图片 ${item.imageName} 吗？`)) return;
  await store.removeImageAction(item.imageId);
}
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-3">
      <div class="flex items-center justify-between gap-4">
        <div>
          <div class="section-kicker">Gallery</div>
          <h3 class="section-title">历史图库</h3>
          <p class="section-copy">支持缩略图查看、放大预览，并可以把历史图片重新载入工作台再次识别。</p>
        </div>
        <button class="secondary-btn" @click="store.refreshWorkspaceData">刷新图库</button>
      </div>

      <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <input class="field-input md:col-span-2 xl:col-span-1" type="text" :value="store.galleryFilters.value.keyword" placeholder="搜索图片名、类别、备注" @input="store.setGalleryFilter('keyword', $event.target.value)" />
        <input class="field-input" type="text" :value="store.galleryFilters.value.className" placeholder="筛选类别，如 plastic" @input="store.setGalleryFilter('className', $event.target.value)" />
        <select class="field-input" :value="store.galleryFilters.value.reviewType" @change="store.setGalleryFilter('reviewType', $event.target.value)">
          <option value="">全部错误类型</option>
          <option value="误检">误检</option>
          <option value="漏检">漏检</option>
          <option value="类别混淆">类别混淆</option>
          <option value="低置信度">低置信度</option>
        </select>
        <button class="secondary-btn" @click="store.refreshWorkspaceData">应用筛选</button>
      </div>

      <div class="mt-4 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <button v-for="item in store.galleryCards.value" :key="item.imageId" class="gallery-item" @click="store.openViewer(item)">
          <img :src="item.imageUrl" :alt="item.imageName" class="h-48 w-full object-cover" />
          <div class="p-4 text-left">
            <div class="flex items-start justify-between gap-3">
              <div class="text-lg font-semibold text-slate-900">{{ item.imageName }}</div>
              <div class="flex gap-2 text-xs">
                <span v-if="item.favorite" class="rounded-full bg-amber-100 px-2 py-1 text-amber-700">收藏</span>
                <span v-if="item.flagged" class="rounded-full bg-rose-100 px-2 py-1 text-rose-700">错误样本</span>
              </div>
            </div>
              <div class="mt-2 text-sm text-slate-500">{{ item.topClass || 'Unknown' }} · {{ item.boxCount }} 个目标</div>
              <div class="mt-1 text-xs text-slate-400">{{ item.imgsz || '--' }} / {{ item.conf ?? '--' }} / {{ item.iou ?? '--' }}</div>
              <div class="mt-2 text-xs text-slate-400">{{ store.formatDate(item.uploadedAt) }}</div>
            </div>
        </button>
      </div>
    </article>

    <div v-if="store.viewerItem.value" class="viewer-mask" @click.self="store.closeViewer()">
      <div class="viewer-card">
        <div class="flex items-start justify-between gap-4">
          <div>
            <div class="section-kicker">Preview</div>
            <h3 class="section-title">{{ store.viewerItem.value.imageName }}</h3>
          </div>
          <button class="secondary-btn" @click="store.closeViewer()">关闭</button>
        </div>

        <div class="mt-6 grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
          <img :src="store.viewerItem.value.imageUrl" :alt="store.viewerItem.value.imageName" class="max-h-[560px] w-full rounded-[28px] object-contain bg-slate-50" />
          <div class="space-y-4">
            <div class="soft-panel">
              <div class="text-sm text-slate-500">主类别</div>
              <div class="mt-2 text-2xl font-semibold text-slate-900">{{ store.viewerItem.value.topClass || 'Unknown' }}</div>
              <div class="mt-2 text-sm text-slate-500">置信度 {{ store.viewerItem.value.topConfidence ? store.formatPercent(store.viewerItem.value.topConfidence) : '--' }}</div>
              <div v-if="store.viewerItem.value.reviewNote" class="mt-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">备注：{{ store.viewerItem.value.reviewNote }}</div>
              <div v-if="store.viewerItem.value.reviewType" class="mt-3 rounded-2xl bg-rose-50 px-4 py-3 text-sm text-slate-600">错误类型：{{ store.viewerItem.value.reviewType }}</div>
              <div v-if="store.viewerItem.value.correctedClass" class="mt-3 rounded-2xl bg-amber-50 px-4 py-3 text-sm text-slate-600">人工修正类别：{{ store.viewerItem.value.correctedClass }}</div>
            </div>
            <div class="soft-panel text-sm leading-7 text-slate-600">
              {{ store.viewerItem.value.advicePreview || '当前还没有建议内容，可以重新载入并生成。' }}
            </div>
            <div class="grid gap-3 sm:grid-cols-3">
              <button class="secondary-btn" @click="toggleFavorite(store.viewerItem.value)">{{ store.viewerItem.value.favorite ? '取消收藏' : '加入收藏' }}</button>
              <button class="secondary-btn" @click="toggleFlag(store.viewerItem.value)">{{ store.viewerItem.value.flagged ? '取消错误标记' : '标记错误样本' }}</button>
              <button class="secondary-btn text-rose-600" @click="removeItem(store.viewerItem.value)">删除图片</button>
            </div>
            <button class="secondary-btn w-full" @click="editMeta(store.viewerItem.value)">编辑错误信息</button>
            <button class="primary-btn w-full" @click="reopen(store.viewerItem.value)">载入到工作台</button>
            <button class="secondary-btn w-full" @click="reopenChat(store.viewerItem.value)">打开对话模块</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
