<script setup>
import { useRouter } from "vue-router";
import { useAppStore } from "../composables/useAppStore";

const router = useRouter();
const store = useAppStore();

function onFileChange(event) {
  const file = event.target.files?.[0];
  if (file) store.setFile(file);
}

function onPreviewLoad(event) {
  store.setPreviewMetrics({
    naturalWidth: event.target.naturalWidth,
    naturalHeight: event.target.naturalHeight,
    clientWidth: event.target.clientWidth,
    clientHeight: event.target.clientHeight
  });
}

async function generateAdvice() {
  const ok = await store.generateAdviceAction();
  if (ok) router.push("/app/assistant");
}
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Workspace</div>
      <h3 class="section-title">识别工作台</h3>
      <p class="section-copy">上传图片后，会直接在图上显示目标框，这样你可以清楚说明每一个垃圾实例对应的识别结果。</p>

      <div class="mt-6 flex flex-col gap-3 xl:flex-row">
        <label class="upload-panel flex-1">
          <span class="text-sm text-slate-600">选择待识别图片</span>
          <input class="mt-3 block w-full text-sm text-slate-600 file:mr-4 file:rounded-full file:border-0 file:bg-emerald-500 file:px-4 file:py-2 file:font-semibold file:text-white" type="file" accept="image/*" @change="onFileChange" />
        </label>
        <button class="primary-btn xl:w-56" :disabled="store.loading.value" @click="store.uploadAndDetectAction">
          {{ store.loading.value ? '识别中...' : '上传并识别' }}
        </button>
      </div>

      <div class="mt-6 grid gap-4 xl:grid-cols-4">
        <label class="soft-panel text-sm text-slate-600">
          输入尺寸
          <input class="field-input mt-3" type="number" min="320" max="1280" step="32" :value="store.detectOptions.value.imgsz" @input="store.setDetectOption('imgsz', Number($event.target.value || 512))" />
        </label>
        <label class="soft-panel text-sm text-slate-600">
          置信度阈值
          <input class="field-input mt-3" type="number" min="0" max="1" step="0.05" :value="store.detectOptions.value.conf" @input="store.setDetectOption('conf', Number($event.target.value || 0.25))" />
        </label>
        <label class="soft-panel text-sm text-slate-600">
          IoU 阈值
          <input class="field-input mt-3" type="number" min="0" max="1" step="0.05" :value="store.detectOptions.value.iou" @input="store.setDetectOption('iou', Number($event.target.value || 0.7))" />
        </label>
        <button class="secondary-btn h-full" :disabled="store.loading.value || !store.imageId.value" @click="store.rerunDetectionAction">
          按当前参数重新识别
        </button>
      </div>

      <div class="mt-4 grid gap-4 xl:grid-cols-3">
        <label class="soft-panel text-sm text-slate-600">
          最小显示置信度
          <input class="field-input mt-3" type="number" min="0" max="1" step="0.05" :value="store.workspaceFilters.value.minConfidence" @input="store.setWorkspaceFilter('minConfidence', Number($event.target.value || 0))" />
        </label>
        <label class="soft-panel text-sm text-slate-600">
          类别筛选
          <input class="field-input mt-3" type="text" :value="store.workspaceFilters.value.classKeyword" placeholder="输入 plastic、metal 等" @input="store.setWorkspaceFilter('classKeyword', $event.target.value)" />
        </label>
        <label class="soft-panel text-sm text-slate-600">
          预览缩放 {{ store.previewScale.value.toFixed(1) }}x
          <input class="mt-4 w-full accent-emerald-500" type="range" min="1" max="2.2" step="0.1" :value="store.previewScale.value" @input="store.setPreviewScale(Number($event.target.value))" />
        </label>
      </div>

      <div class="mt-4 flex flex-wrap gap-3">
        <input :value="store.experimentNote.value" class="field-input max-w-xl" type="text" placeholder="实验备注，例如：提高 conf 后误检减少" @input="store.experimentNote.value = $event.target.value" />
        <button class="secondary-btn" @click="store.saveExperimentRun">保存当前实验结果</button>
        <div class="soft-panel text-sm text-slate-600">已记录实验 {{ store.experimentRuns.value.length }} 组</div>
      </div>

      <div class="mt-6 image-stage">
        <div v-if="store.previewUrl.value" class="preview-canvas" :style="{ transform: `scale(${store.previewScale.value})` }">
          <img :src="store.previewUrl.value" class="max-h-[460px] max-w-full object-contain" @load="onPreviewLoad" />

          <div
            v-for="box in store.overlayBoxes.value"
            :key="`${box.className}-${box.index}`"
            class="absolute rounded-2xl border-2"
            :class="store.selectedDetectionIndex.value === box.index ? 'ring-4 ring-white/70' : ''"
            :style="{ left: `${box.left}px`, top: `${box.top}px`, width: `${Math.max(box.boxWidth, 28)}px`, height: `${Math.max(box.boxHeight, 28)}px`, borderColor: box.color }"
          >
            <div class="absolute -top-7 left-0 rounded-full px-3 py-1 text-[11px] font-semibold text-slate-900" :style="{ backgroundColor: box.color }">
              {{ box.className }} · {{ store.formatPercent(box.confidence) }}
            </div>
          </div>
        </div>

        <div v-if="!store.previewUrl.value" class="px-6 text-center text-slate-400">
          上传图片后，这里会显示实例框定位结果。
        </div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Instances</div>
      <h3 class="section-title">实例列表</h3>
      <div class="mt-5 space-y-3">
        <button v-for="(box, index) in store.detectionList.value" :key="`${box.className}-${index}`" class="instance-card" :class="store.selectedDetectionIndex.value === index ? 'instance-card-active' : ''" @click="store.selectDetection(index)">
          <div class="flex items-center justify-between gap-3">
            <div>
              <div class="font-semibold text-slate-900">{{ box.className }}</div>
              <div class="mt-1 text-xs text-slate-500">实例 #{{ index + 1 }}</div>
            </div>
            <div class="rounded-full bg-white px-3 py-1 text-xs text-slate-700 shadow-sm">
              {{ store.formatPercent(box.confidence) }}
            </div>
          </div>
        </button>
        <div v-if="!store.detectionList.value.length" class="soft-panel text-sm text-slate-500">识别后，这里会出现实例列表。</div>
      </div>

      <div v-if="store.selectedDetection.value" class="soft-panel mt-5 text-sm text-slate-600">
        <div class="text-xs uppercase tracking-[0.2em] text-slate-400">实例详情</div>
        <div class="mt-3 text-lg font-semibold text-slate-900">{{ store.selectedDetection.value.className }}</div>
        <div class="mt-2">分类归属：<span class="font-semibold" :style="{ color: store.selectedDetectionMeta.value?.color }">{{ store.selectedDetectionMeta.value?.category }}</span></div>
        <div class="mt-2">置信度等级：{{ store.selectedDetectionMeta.value?.confidenceLevel }}（{{ store.formatPercent(store.selectedDetection.value.confidence) }}）</div>
        <div class="mt-2">提示：{{ store.selectedDetectionMeta.value?.confidenceHint }}</div>
        <div class="mt-2">投放建议：{{ store.selectedDetectionMeta.value?.tips }}</div>
      </div>

      <div v-if="store.detectionSummary.value.total" class="soft-panel mt-5 text-sm text-slate-600">
        <div class="text-xs uppercase tracking-[0.2em] text-slate-400">识别摘要</div>
        <div class="mt-3">本图共识别 {{ store.detectionSummary.value.total }} 个目标，低置信度目标 {{ store.detectionSummary.value.lowConfidenceCount }} 个。</div>
        <div class="mt-3 space-y-2">
          <div v-for="item in store.detectionSummary.value.byCategory" :key="item.className" class="flex items-center justify-between gap-3 rounded-2xl bg-slate-50 px-3 py-2">
            <div>
              <div class="font-semibold text-slate-900">{{ item.className }}</div>
              <div class="text-xs text-slate-500">{{ item.wasteCategory }}</div>
            </div>
            <div class="text-sm text-slate-500">{{ item.count }} 个实例</div>
          </div>
        </div>
      </div>

      <button class="secondary-btn mt-4 w-full" @click="store.resetWorkspaceFilters">重置筛选和缩放</button>

      <textarea v-model="store.extraContext.value" rows="4" class="field-input mt-5" placeholder="补充说明，例如：是否有油污、是否在教室或食堂场景拍摄..." />
      <button class="secondary-btn mt-4 w-full" :disabled="store.loading.value || !store.detection.value" @click="generateAdvice">生成回收建议</button>
    </article>
  </section>
</template>
