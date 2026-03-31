<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAppStore } from "../composables/useAppStore";

const router = useRouter();
const store = useAppStore();
const mode = ref("login");
const form = reactive({ username: "", email: "", displayName: "", password: "" });

async function submit() {
  const ok = mode.value === "login"
    ? await store.loginAction({ username: form.username, password: form.password })
    : await store.registerAction({ username: form.username, email: form.email, displayName: form.displayName, password: form.password });
  if (ok) {
    router.replace("/app/overview");
  }
}
</script>

<template>
  <div class="login-wrap">
    <section class="login-hero">
      <div class="max-w-2xl">
        <div class="brand-pill">Waste Vision Platform</div>
        <h1 class="mt-6 font-display text-5xl leading-tight text-slate-900 md:text-6xl">
          先登录，再进入你的垃圾识别与回收指导系统
        </h1>
        <p class="mt-5 max-w-xl text-base leading-8 text-slate-600">
          这一版把登录页独立成系统外层入口，整体视觉改成更清新、明亮、适合展示和答辩的产品界面。
        </p>
        <div class="mt-8 flex flex-wrap gap-3">
          <span class="soft-badge">账号系统</span>
          <span class="soft-badge">历史图库</span>
          <span class="soft-badge">AI 回收对话</span>
          <span class="soft-badge">实例定位展示</span>
        </div>
      </div>

      <div class="login-card login-card-right">
        <div class="inline-flex rounded-full bg-slate-100 p-1">
          <button class="auth-switch" :class="mode === 'login' ? 'auth-switch-active' : ''" @click="mode = 'login'">登录</button>
          <button class="auth-switch" :class="mode === 'register' ? 'auth-switch-active' : ''" @click="mode = 'register'">注册</button>
        </div>

        <div class="mt-6 space-y-4">
          <input v-model="form.username" class="field-input" :placeholder="mode === 'login' ? '用户名或邮箱' : '用户名'" />
          <input v-if="mode === 'register'" v-model="form.displayName" class="field-input" placeholder="显示名称" />
          <input v-if="mode === 'register'" v-model="form.email" class="field-input" placeholder="邮箱" />
          <input v-model="form.password" type="password" class="field-input" placeholder="密码" />
          <button class="primary-btn w-full" :disabled="store.authLoading.value" @click="submit">
            {{ store.authLoading.value ? '提交中...' : mode === 'login' ? '进入系统' : '创建账号' }}
          </button>
        </div>

        <div v-if="store.error.value" class="mt-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-600">
          {{ store.error.value }}
        </div>

        <div class="mt-6 rounded-[24px] bg-emerald-50 p-4 text-sm leading-7 text-slate-600">
          测试账号也可以继续使用：`demo_user / demo1234`
        </div>
      </div>
    </section>
  </div>
</template>
