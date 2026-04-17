<script setup>
import { ref } from "vue";
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();
const selectedModel = ref(null);

const modelHighlights = [
  { label: "当前版本", value: () => store.currentModelInfo.value.modelVersion || "未加载" },
  { label: "已归档模型", value: () => String(store.availableModels.value.length) },
  { label: "支持格式", value: () => ".pt / .onnx" }
];

function onFileChange(event) {
  selectedModel.value = event.target.files?.[0] || null;
}

async function uploadModel() {
  await store.uploadModelAction(selectedModel.value);
}

async function useModel(item) {
  await store.useModelAction(item.modelPath);
}
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Models</div>
      <h3 class="section-title">模型管理</h3>
      <p class="section-copy">集中查看当前推理版本、补充新模型文件，并在历史版本之间快速切换。</p>

      <div class="model-hero mt-6">
        <div class="model-hero-main">
          <div class="model-hero-label model-label-with-icon">
            <span class="model-inline-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 4 4.5 8 12 12 19.5 8 12 4zm-7.5 8L12 16l7.5-4M4.5 12l7.5 4 7.5-4" />
              </svg>
            </span>
            Active Model
          </div>
          <div class="model-hero-title">{{ store.currentModelInfo.value.modelVersion || '未加载模型' }}</div>
          <div class="model-hero-path">{{ store.currentModelInfo.value.modelPath || '暂无模型路径信息' }}</div>
        </div>
        <div class="model-hero-stats">
          <div v-for="item in modelHighlights" :key="item.label" class="model-stat-card">
            <div class="model-stat-label">{{ item.label }}</div>
            <div class="model-stat-value">{{ item.value() }}</div>
          </div>
        </div>
      </div>

      <div class="model-upload-panel mt-6">
        <label class="upload-panel model-upload-field">
          <span class="text-sm text-slate-600">选择模型文件</span>
          <div class="model-upload-copy">拖入或选择新的权重文件，上传后立即可切换为当前推理版本。</div>
          <input class="mt-4 block w-full text-sm text-slate-600 file:mr-4 file:rounded-full file:border-0 file:bg-emerald-500 file:px-4 file:py-2 file:font-semibold file:text-white" type="file" accept=".pt,.onnx" @change="onFileChange" />
        </label>
        <button class="primary-btn model-upload-btn" :disabled="store.modelUploadLoading.value" @click="uploadModel">
          {{ store.modelUploadLoading.value ? '上传中...' : '上传并启用模型' }}
        </button>
      </div>

      <div class="mt-8">
        <div class="section-kicker">History</div>
        <h3 class="section-title">历史模型</h3>
        <div class="mt-4 grid gap-4 model-history-grid">
          <div v-for="item in store.availableModels.value" :key="item.modelPath" class="model-record-card">
            <div class="model-record-head">
              <div class="model-record-badge">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 4 4.5 8 12 12 19.5 8 12 4zm-7.5 8L12 16l7.5-4" />
                </svg>
              </div>
              <div>
                <div class="font-semibold text-slate-900">{{ item.modelVersion }}</div>
                <div class="model-record-tag">模型归档</div>
              </div>
            </div>
            <div class="mt-4 break-all text-xs text-slate-500">{{ item.modelPath }}</div>
            <div class="model-record-actions">
              <div class="model-record-meta">可作为当前推理版本启用</div>
              <button class="secondary-btn" :disabled="store.modelUploadLoading.value" @click="useModel(item)">
                切换到此模型
              </button>
            </div>
          </div>
          <div v-if="!store.availableModels.value.length" class="soft-panel text-sm text-slate-500">当前还没有历史模型记录。</div>
        </div>
      </div>
    </article>
  </section>
</template>
