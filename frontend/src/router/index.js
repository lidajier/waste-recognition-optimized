import { createRouter, createWebHistory } from "vue-router";
import { getToken } from "../lib/api";
import AppShell from "../layouts/AppShell.vue";
import AssistantView from "../views/AssistantView.vue";
import ExperimentsView from "../views/ExperimentsView.vue";
import GalleryView from "../views/GalleryView.vue";
import LoginView from "../views/LoginView.vue";
import ModelsView from "../views/ModelsView.vue";
import OverviewView from "../views/OverviewView.vue";
import ReviewView from "../views/ReviewView.vue";
import WorkspaceView from "../views/WorkspaceView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: () => (getToken() ? "/app/overview" : "/login")
    },
    {
      path: "/login",
      component: LoginView,
      meta: { guestOnly: true }
    },
    {
      path: "/app",
      component: AppShell,
      meta: { requiresAuth: true },
      children: [
        { path: "", redirect: "/app/overview" },
        { path: "overview", component: OverviewView },
        { path: "workspace", component: WorkspaceView },
        { path: "assistant", component: AssistantView },
        { path: "models", component: ModelsView },
        { path: "gallery", component: GalleryView },
        { path: "review", component: ReviewView },
        { path: "experiments", component: ExperimentsView }
      ]
    }
  ]
});

router.beforeEach((to) => {
  const token = getToken();
  if (to.meta.requiresAuth && !token) {
    return "/login";
  }
  if (to.meta.guestOnly && token) {
    return "/app/overview";
  }
  return true;
});

export default router;
