<script setup>
import { useAppStore } from "../composables/useAppStore";

const store = useAppStore();

const presets = [
  "这个垃圾属于哪一类？",
  "投放前需要清洗吗？",
  "如果是校园外卖包装应该怎么处理？",
  "有哪些投放注意事项？"
];

function usePreset(text) {
  store.question.value = text;
}
</script>

<template>
  <section class="page-grid">
    <article class="content-card lg:col-span-2">
      <div class="section-kicker">Assistant</div>
      <h3 class="section-title">AI 回收对话</h3>
      <p class="section-copy">这一页单独作为大模型问答模块，视觉和功能上都不再像附属面板。</p>

      <div class="chat-board mt-6">
        <div v-for="(message, idx) in store.chatMessages.value" :key="`${message.createdAt}-${idx}`" class="flex" :class="message.role === 'assistant' ? 'justify-start' : 'justify-end'">
          <div class="chat-bubble" :class="message.role === 'assistant' ? 'chat-ai' : 'chat-user'">
            <div class="mb-2 text-[11px] uppercase tracking-[0.24em] opacity-60">{{ message.role === 'assistant' ? 'AI 顾问' : '用户' }}</div>
            <pre class="whitespace-pre-wrap">{{ message.content }}</pre>
          </div>
        </div>
        <div v-if="!store.chatMessages.value.length" class="soft-panel text-sm text-slate-500">先在工作台完成识别并生成建议，再来这里继续追问。</div>
      </div>
    </article>

    <article class="content-card">
      <div class="section-kicker">Follow-up</div>
      <h3 class="section-title">继续追问</h3>
      <div class="mt-5 grid gap-2">
        <button v-for="item in presets" :key="item" class="secondary-btn text-left" @click="usePreset(item)">{{ item }}</button>
      </div>
      <textarea v-model="store.question.value" rows="8" class="field-input mt-5" placeholder="例如：这个塑料盒需要清洗吗？可回收垃圾应该怎么投放？" />
      <button class="primary-btn mt-4 w-full" :disabled="store.chatLoading.value || !store.sessionId.value" @click="store.askFollowupAction">
        {{ store.chatLoading.value ? '发送中...' : '发送问题' }}
      </button>
    </article>
  </section>
</template>
