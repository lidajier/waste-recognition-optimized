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
      <div class="login-branding max-w-2xl">
        <div class="brand-pill">Circular Campus</div>
        <h1 class="mt-6 font-display text-5xl leading-tight text-slate-900 md:text-6xl">
          垃圾识别与智能回收指导系统
        </h1>
        <p class="mt-5 max-w-xl text-base leading-8 text-slate-600">
          校园场景回收演示平台
        </p>
        <div class="login-badge-row mt-8">
          <span class="soft-badge">Detection</span>
          <span class="soft-badge">Guidance</span>
          <span class="soft-badge">Archive</span>
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

        <div class="login-footnote mt-6">
          demo_user / demo1234
        </div>
      </div>
    </section>
  </div>
</template>
