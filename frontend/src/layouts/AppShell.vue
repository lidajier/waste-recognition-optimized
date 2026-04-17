<script setup>
import { onMounted } from "vue";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { useAppStore } from "../composables/useAppStore";

const router = useRouter();
const store = useAppStore();

const navItems = [
  { to: "/app/overview", label: "总览", icon: "overview" },
  { to: "/app/workspace", label: "识别工作台", icon: "workspace" },
  { to: "/app/assistant", label: "AI 对话", icon: "assistant" },
  { to: "/app/models", label: "模型管理", icon: "models" },
  { to: "/app/gallery", label: "历史图库", icon: "gallery" },
  { to: "/app/review", label: "错误样本", icon: "review" },
  { to: "/app/experiments", label: "模型实验", icon: "experiments" }
];

const statusChips = [
  "本地 YOLO 推理",
  "校园垃圾分类",
  "多轮回收问答"
];

const quickPanels = [
  { title: "识别", meta: "实例框与类别摘要" },
  { title: "建议", meta: "本地代理模型生成" },
  { title: "归档", meta: "图库与实验记录" }
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

function iconPath(name) {
  const paths = {
    overview: "M4 5.5h7v5H4zM13 5.5h7v8h-7zM4 12.5h7v6H4zM13 15.5h7v3h-7z",
    workspace: "M4 6.5h16M7 4v5M17 4v5M6 13l3 3 9-9",
    assistant: "M6 8.5a6 6 0 0 1 12 0v4a6 6 0 0 1-6 6h-1l-4 2v-3.2A6 6 0 0 1 6 12.5z",
    models: "M12 4 4.5 8 12 12 19.5 8 12 4zm-7.5 8L12 16l7.5-4M4.5 12l7.5 4 7.5-4",
    gallery: "M5 6.5h14a1.5 1.5 0 0 1 1.5 1.5v8A1.5 1.5 0 0 1 19 17.5H5A1.5 1.5 0 0 1 3.5 16V8A1.5 1.5 0 0 1 5 6.5zm2 7 2.7-2.7 2.3 2.3 3.5-3.5 2.5 4.4",
    review: "M12 4.5 19 8v8l-7 3.5L5 16V8l7-3.5zm0 4.5v4m0 3h.01",
    experiments: "M7 4.5h10M9 4.5v5.2l-3.8 6.1A1.5 1.5 0 0 0 6.5 18h11a1.5 1.5 0 0 0 1.3-2.2L15 9.7V4.5"
  };
  return paths[name] || paths.overview;
}
</script>

<template>
  <div class="app-wrap">
    <aside class="sidebar-card">
      <div class="sidebar-logo-wrap">
        <div class="sidebar-logo-mark" aria-hidden="true">
          <span class="sidebar-logo-dot sidebar-logo-dot-a"></span>
          <span class="sidebar-logo-dot sidebar-logo-dot-b"></span>
          <span class="sidebar-logo-core">R</span>
        </div>
        <div>
          <div class="sidebar-logo-title">Circular Campus</div>
          <div class="sidebar-logo-subtitle">垃圾识别与回收指导</div>
        </div>
      </div>

      <div class="sidebar-story">
        <div class="sidebar-story-label">Project Lens</div>
        <p class="sidebar-story-copy">围绕识别、建议和归档三个核心面板组织整套演示界面。</p>
        <div class="sidebar-mini-grid">
          <div v-for="panel in quickPanels" :key="panel.title" class="sidebar-mini-card">
            <div class="sidebar-mini-icon"></div>
            <div>
              <div class="sidebar-mini-title">{{ panel.title }}</div>
              <div class="sidebar-mini-meta">{{ panel.meta }}</div>
            </div>
          </div>
        </div>
      </div>

      <nav class="space-y-2">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link">
          <span class="nav-link-inner">
            <span class="nav-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                <path :d="iconPath(item.icon)" />
              </svg>
            </span>
            <span>{{ item.label }}</span>
          </span>
        </RouterLink>
      </nav>

    </aside>

    <div class="main-stage">
      <header class="topbar-card">
        <div class="topbar-head">
          <div class="topbar-copy">
            <p class="topbar-kicker">Graduation Project</p>
            <h2 class="topbar-title">垃圾识别与智能回收指导系统</h2>
            <p class="topbar-subtitle">用于答辩展示的校园回收控制台，聚焦图片识别、回收建议与结果留档。</p>
            <div class="status-strip">
              <span v-for="chip in statusChips" :key="chip" class="status-chip">
                <span class="status-chip-dot"></span>
                {{ chip }}
              </span>
            </div>
          </div>
          <div class="topbar-user">
            <div class="user-panel">
              <div class="user-panel-badge">在线</div>
              <div class="user-pill">
                <div class="user-avatar">{{ (store.currentUser.value?.displayName || "G").slice(0, 1) }}</div>
                <div class="user-meta">
                  <div class="text-sm font-semibold text-slate-900">{{ store.currentUser.value?.displayName || "访客" }}</div>
                  <div class="text-xs text-slate-500">@{{ store.currentUser.value?.username || "guest" }}</div>
                </div>
                <div class="user-role-chip">
                  <span class="user-role-dot"></span>
                  控制台用户
                </div>
              </div>
              <button class="secondary-btn user-action-btn" @click="logout">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                  <path d="M16 17l5-5-5-5" />
                  <path d="M21 12H9" />
                </svg>
                退出
              </button>
            </div>
          </div>
        </div>

        <div class="metric-grid">
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
