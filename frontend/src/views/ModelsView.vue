<script setup>
import { ref } from "vue";
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();
const selectedModel = ref(null);

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
      <p class="section-copy">支持上传新的 YOLO 模型文件，上传完成后立即切换为当前推理模型。</p>

      <div class="mt-6 soft-panel text-sm text-slate-600">
        <div>当前模型：<span class="font-semibold text-slate-900">{{ store.currentModelInfo.value.modelVersion || '未加载' }}</span></div>
        <div class="mt-2 break-all text-xs text-slate-500">{{ store.currentModelInfo.value.modelPath || '暂无模型路径信息' }}</div>
      </div>

      <div class="mt-6 flex flex-col gap-3 xl:flex-row">
        <label class="upload-panel flex-1">
          <span class="text-sm text-slate-600">选择模型文件（.pt / .onnx）</span>
          <input class="mt-3 block w-full text-sm text-slate-600 file:mr-4 file:rounded-full file:border-0 file:bg-emerald-500 file:px-4 file:py-2 file:font-semibold file:text-white" type="file" accept=".pt,.onnx" @change="onFileChange" />
        </label>
        <button class="primary-btn xl:w-56" :disabled="store.modelUploadLoading.value" @click="uploadModel">
          {{ store.modelUploadLoading.value ? '上传中...' : '上传并启用模型' }}
        </button>
      </div>

      <div class="mt-8">
        <div class="section-kicker">History</div>
        <h3 class="section-title">历史模型</h3>
        <div class="mt-4 grid gap-4">
          <div v-for="item in store.availableModels.value" :key="item.modelPath" class="soft-panel flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
            <div>
              <div class="font-semibold text-slate-900">{{ item.modelVersion }}</div>
              <div class="mt-2 break-all text-xs text-slate-500">{{ item.modelPath }}</div>
            </div>
            <button class="secondary-btn" :disabled="store.modelUploadLoading.value" @click="useModel(item)">
              设为当前模型
            </button>
          </div>
          <div v-if="!store.availableModels.value.length" class="soft-panel text-sm text-slate-500">暂无历史模型记录。</div>
        </div>
      </div>
    </article>
  </section>
</template>
