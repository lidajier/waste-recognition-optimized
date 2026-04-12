<script setup>
import { onMounted } from "vue";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { useAppStore } from "../composables/useAppStore";

const router = useRouter();
const store = useAppStore();

const navItems = [
  { to: "/app/overview", label: "总览" },
  { to: "/app/workspace", label: "识别工作台" },
  { to: "/app/assistant", label: "AI 对话" },
  { to: "/app/gallery", label: "历史图库" },
  { to: "/app/review", label: "错误样本" },
  { to: "/app/experiments", label: "模型实验" }
];

onMounted(async () => {
  await store.bootstrap();
  if (!store.isAuthenticated.value) {
    router.replace("/login");
  }
});

function logout() {
  store.logoutAction();
  router.replace("/login");
}
</script>

<template>
  <div class="app-wrap">
    <aside class="sidebar-card">
      <nav class="space-y-2">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link">
          {{ item.label }}
        </RouterLink>
      </nav>

    </aside>

    <div class="main-stage">
      <header class="topbar-card">
        <div class="topbar-head">
          <div>
            <p class="text-xs uppercase tracking-[0.28em] text-emerald-600">Graduation Project</p>
            <h2 class="mt-2 font-display text-4xl text-slate-900">垃圾识别与智能回收指导系统</h2>
          </div>
          <div class="topbar-user">
            <div class="user-pill">
              <div class="user-meta">
                <div class="text-sm font-semibold text-slate-900">{{ store.currentUser.value?.displayName || "访客" }}</div>
                <div class="text-xs text-slate-500">@{{ store.currentUser.value?.username || "guest" }}</div>
              </div>
              <div class="user-avatar">{{ (store.currentUser.value?.displayName || "G").slice(0, 1) }}</div>
            </div>
            <button class="secondary-btn" @click="logout">退出登录</button>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <div v-for="card in store.dashboardCards.value" :key="card.label" class="metric-card">
            <div class="text-xs uppercase tracking-[0.2em] text-slate-400">{{ card.label }}</div>
            <div class="mt-3 text-2xl font-semibold text-slate-900">{{ card.value }}</div>
            <div class="mt-2 text-xs leading-5 text-slate-500">{{ card.hint }}</div>
          </div>
        </div>
      </header>

      <RouterView v-slot="{ Component }">
        <Transition name="shell-fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </div>
  </div>
</template>
