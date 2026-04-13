import { computed, ref } from "vue";
import {
  clearToken,
  createExperiment,
  deleteImage,
  deleteExperiment,
  generateAdvice,
  getGallery,
  getHistory,
  getCurrentModel,
  getMe,
  getOverviewStats,
  getSession,
  getToken,
  listModels,
  listExperiments,
  login,
  register,
  runDetection,
  sendChat,
  setToken,
  updateImage,
  uploadModel,
  useModel,
  uploadImage
} from "../lib/api";
import { classifyWaste, confidenceHint, confidenceLevel } from "../lib/wasteMeta";

const selectedFile = ref(null);
const previewUrl = ref("");
const imageId = ref("");
const detection = ref(null);
const sessionId = ref("");
const chatMessages = ref([]);
const historyItems = ref([]);
const galleryItems = ref([]);
const question = ref("");
const extraContext = ref("");
const loading = ref(false);
const chatLoading = ref(false);
const error = ref("");
const currentUser = ref(null);
const authLoading = ref(false);
const bootstrapped = ref(false);
const selectedDetectionIndex = ref(0);
const naturalSize = ref({ width: 1, height: 1 });
const renderedSize = ref({ width: 1, height: 1 });
const viewerItem = ref(null);
const detectOptions = ref({ imgsz: 512, conf: 0.25, iou: 0.7 });
const workspaceFilters = ref({ minConfidence: 0, classKeyword: "" });
const previewScale = ref(1);
const experimentRuns = ref([]);
const experimentNote = ref("");
const overviewStats = ref({
  totalImages: 0,
  totalDetections: 0,
  totalDetectedObjects: 0,
  flaggedCount: 0,
  favoriteCount: 0,
  avgLatencyMs: 0,
  topClass: "暂无数据",
  topClasses: [],
  recentActivities: []
});
const galleryFilters = ref({
  keyword: "",
  className: "",
  favorite: "",
  flagged: "",
  reviewType: "",
  minConfidence: 0
});
const currentModelInfo = ref({ modelPath: "", modelVersion: "未加载" });
const availableModels = ref([]);
const modelUploadLoading = ref(false);

const BOX_COLORS = ["#52c7a5", "#ffbf69", "#6ea8fe", "#ff8a8a", "#b692ff", "#66d9ef"];
const GALLERY_ACCENTS = ["from-emerald-100 to-teal-50", "from-amber-100 to-orange-50", "from-sky-100 to-cyan-50"];

const rawDetectionList = computed(() => detection.value?.detections || []);
const detectionList = computed(() => rawDetectionList.value.filter((item) => {
  const matchesConfidence = item.confidence >= workspaceFilters.value.minConfidence;
  const keyword = workspaceFilters.value.classKeyword.trim().toLowerCase();
  const matchesClass = !keyword || item.className.toLowerCase().includes(keyword);
  return matchesConfidence && matchesClass;
}));
const selectedDetection = computed(() => detectionList.value[selectedDetectionIndex.value] || null);
const isAuthenticated = computed(() => !!currentUser.value);

const stats = computed(() => {
  const list = detectionList.value;
  if (!list.length) {
    return { count: 0, top: "等待识别", avgConfidence: 0, lowConfidenceCount: 0 };
  }
  const top = [...list].sort((a, b) => b.confidence - a.confidence)[0];
  return {
    count: list.length,
    top: `${top.className} (${formatPercent(top.confidence)})`,
    avgConfidence: list.reduce((sum, item) => sum + item.confidence, 0) / list.length,
    lowConfidenceCount: list.filter((item) => item.confidence < 0.45).length
  };
});

const classSummary = computed(() => {
  const summary = new Map();
  detectionList.value.forEach((item, index) => {
    const current = summary.get(item.className) || { className: item.className, count: 0, bestConfidence: 0, indexes: [] };
    current.count += 1;
    current.bestConfidence = Math.max(current.bestConfidence, item.confidence);
    current.indexes.push(index);
    summary.set(item.className, current);
  });
  return [...summary.values()].sort((a, b) => b.count - a.count || b.bestConfidence - a.bestConfidence);
});

const historySummary = computed(() => {
  const total = galleryItems.value.length;
  const avgLatency = total ? Math.round(galleryItems.value.reduce((sum, item) => sum + (item.latencyMs || 0), 0) / total) : 0;
  return { total, avgLatency };
});

const flaggedItems = computed(() => galleryCards.value.filter((item) => item.flagged));
const favoriteItems = computed(() => galleryCards.value.filter((item) => item.favorite));

const selectedDetectionMeta = computed(() => {
  const item = selectedDetection.value;
  if (!item) return null;
  const waste = classifyWaste(item.className);
  return {
    ...waste,
    confidenceLevel: confidenceLevel(item.confidence),
    confidenceHint: confidenceHint(item.confidence)
  };
});

const detectionSummary = computed(() => {
  const byCategory = classSummary.value.map((item) => ({
    ...item,
    wasteCategory: classifyWaste(item.className).category
  }));
  return {
    total: detectionList.value.length,
    lowConfidenceCount: stats.value.lowConfidenceCount,
    byCategory
  };
});

const galleryCards = computed(() => galleryItems.value.map((item, index) => ({
  ...item,
  imageUrl: withToken(item.imageUrl),
  accent: GALLERY_ACCENTS[index % GALLERY_ACCENTS.length]
})));

const overlayBoxes = computed(() => {
  const width = renderedSize.value.width || 1;
  const height = renderedSize.value.height || 1;
  const naturalWidth = naturalSize.value.width || 1;
  const naturalHeight = naturalSize.value.height || 1;
  return detectionList.value.map((item, index) => ({
    ...item,
    index,
    left: (item.x1 / naturalWidth) * width,
    top: (item.y1 / naturalHeight) * height,
    boxWidth: ((item.x2 - item.x1) / naturalWidth) * width,
    boxHeight: ((item.y2 - item.y1) / naturalHeight) * height,
    color: BOX_COLORS[index % BOX_COLORS.length]
  }));
});

const dashboardCards = computed(() => [
  { label: "累计图片数", value: String(overviewStats.value.totalImages || 0), hint: "系统已保存的图片记录" },
  { label: "累计识别目标", value: String(overviewStats.value.totalDetectedObjects || 0), hint: "用于体现系统处理规模" },
  { label: "错误样本数", value: String(overviewStats.value.flaggedCount || 0), hint: "支持误检漏检回流分析" },
  { label: "平均推理时延", value: `${overviewStats.value.avgLatencyMs || 0} ms`, hint: "适合作为论文性能指标" }
]);

function formatPercent(value) {
  return `${((value || 0) * 100).toFixed(1)}%`;
}

function formatDate(value) {
  if (!value) return "--";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function withToken(url) {
  if (!url) return "";
  const token = getToken();
  if (!token) return url;
  const separator = url.includes("?") ? "&" : "?";
  return `${url}${separator}token=${encodeURIComponent(token)}`;
}

function setError(message) {
  error.value = message || "";
}

function clearPreviewUrl() {
  if (previewUrl.value && previewUrl.value.startsWith("blob:")) {
    URL.revokeObjectURL(previewUrl.value);
  }
}

function resetSession() {
  detection.value = null;
  sessionId.value = "";
  chatMessages.value = [];
  question.value = "";
  extraContext.value = "";
  selectedDetectionIndex.value = 0;
}

function resetWorkspaceFilters() {
  workspaceFilters.value = { minConfidence: 0, classKeyword: "" };
  previewScale.value = 1;
}

async function refreshWorkspaceData() {
  if (!isAuthenticated.value) {
    historyItems.value = [];
    galleryItems.value = [];
    experimentRuns.value = [];
    return;
  }
  const [history, gallery, statsResp, experimentsResp] = await Promise.all([
    getHistory({ limit: 12 }),
    getGallery({ limit: 24, ...buildGalleryQuery() }),
    getOverviewStats(),
    listExperiments()
  ]);
  historyItems.value = history.items || [];
  galleryItems.value = gallery.items || [];
  overviewStats.value = statsResp || overviewStats.value;
  experimentRuns.value = experimentsResp.items || [];
  try {
    currentModelInfo.value = await getCurrentModel();
    availableModels.value = await listModels();
  } catch {
    currentModelInfo.value = { modelPath: "", modelVersion: "未加载" };
    availableModels.value = [];
  }
}

function buildGalleryQuery() {
  return {
    keyword: galleryFilters.value.keyword || undefined,
    className: galleryFilters.value.className || undefined,
    favorite: galleryFilters.value.favorite === "" ? undefined : galleryFilters.value.favorite === "true",
    flagged: galleryFilters.value.flagged === "" ? undefined : galleryFilters.value.flagged === "true",
    reviewType: galleryFilters.value.reviewType || undefined,
    minConfidence: galleryFilters.value.minConfidence || undefined
  };
}

async function bootstrap() {
  if (bootstrapped.value) return;
  if (!getToken()) {
    bootstrapped.value = true;
    return;
  }
  try {
    currentUser.value = await getMe();
    await refreshWorkspaceData();
  } catch {
    clearToken();
    currentUser.value = null;
  } finally {
    bootstrapped.value = true;
  }
}

async function loginAction(payload) {
  authLoading.value = true;
  error.value = "";
  try {
    const response = await login(payload);
    setToken(response.token);
    currentUser.value = response.user;
    await refreshWorkspaceData();
    return true;
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "登录失败。");
    return false;
  } finally {
    authLoading.value = false;
  }
}

async function registerAction(payload) {
  authLoading.value = true;
  error.value = "";
  try {
    const response = await register(payload);
    setToken(response.token);
    currentUser.value = response.user;
    await refreshWorkspaceData();
    return true;
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "注册失败。");
    return false;
  } finally {
    authLoading.value = false;
  }
}

function logoutAction() {
  clearToken();
  currentUser.value = null;
  historyItems.value = [];
  galleryItems.value = [];
  experimentRuns.value = [];
  viewerItem.value = null;
  clearPreviewUrl();
  previewUrl.value = "";
  selectedFile.value = null;
  imageId.value = "";
  resetSession();
}

function setFile(file) {
  if (!file) return;
  selectedFile.value = file;
  imageId.value = "";
  resetSession();
  resetWorkspaceFilters();
  clearPreviewUrl();
  previewUrl.value = URL.createObjectURL(file);
}

function setPreviewMetrics({ naturalWidth, naturalHeight, clientWidth, clientHeight }) {
  naturalSize.value = { width: naturalWidth || 1, height: naturalHeight || 1 };
  renderedSize.value = { width: clientWidth || 1, height: clientHeight || 1 };
}

async function uploadAndDetectAction() {
  if (!isAuthenticated.value) {
    setError("请先登录后再上传图片。");
    return;
  }
  if (!selectedFile.value) {
    setError("请先选择一张垃圾图片。");
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    const uploaded = await uploadImage(selectedFile.value);
    imageId.value = uploaded.imageId;
    previewUrl.value = withToken(uploaded.imageUrl);
    detection.value = await runDetection(uploaded.imageId, detectOptions.value);
    selectedDetectionIndex.value = 0;
    await refreshWorkspaceData();
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "上传或识别失败。");
  } finally {
    loading.value = false;
  }
}

async function generateAdviceAction() {
  if (!detection.value?.detectionId) {
    setError("当前还没有可用的识别结果。");
    return false;
  }
  loading.value = true;
  error.value = "";
  try {
    const result = await generateAdvice(detection.value.detectionId, extraContext.value);
    sessionId.value = result.sessionId;
    await refreshSession();
    await refreshWorkspaceData();
    return true;
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "回收建议生成失败。");
    return false;
  } finally {
    loading.value = false;
  }
}

async function refreshSession() {
  if (!sessionId.value) return;
  const session = await getSession(sessionId.value);
  chatMessages.value = session.messages || [];
}

async function askFollowupAction() {
  if (!sessionId.value || !question.value.trim()) return;
  chatLoading.value = true;
  error.value = "";
  try {
    await sendChat(sessionId.value, question.value.trim());
    question.value = "";
    await refreshSession();
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "追问失败。");
  } finally {
    chatLoading.value = false;
  }
}

function selectDetection(index) {
  selectedDetectionIndex.value = index;
}

function openViewer(item) {
  viewerItem.value = item;
}

function closeViewer() {
  viewerItem.value = null;
}

async function reopenFromGallery(item) {
  try {
    loading.value = true;
    error.value = "";
    clearPreviewUrl();
    previewUrl.value = withToken(item.imageUrl);
    imageId.value = item.imageId;
    if (item.detectionId) {
      detection.value = await runDetection(item.imageId, detectOptions.value);
      selectedDetectionIndex.value = 0;
    }
    if (item.sessionId) {
      sessionId.value = item.sessionId;
      await refreshSession();
    }
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "历史记录载入失败。");
  } finally {
    loading.value = false;
  }
}

async function rerunDetectionAction() {
  if (!imageId.value) {
    setError("当前没有可重新识别的图片。");
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    detection.value = await runDetection(imageId.value, detectOptions.value);
    selectedDetectionIndex.value = 0;
    await refreshWorkspaceData();
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "重新识别失败。");
  } finally {
    loading.value = false;
  }
}

function setDetectOption(key, value) {
  detectOptions.value = { ...detectOptions.value, [key]: value };
}

function setWorkspaceFilter(key, value) {
  workspaceFilters.value = { ...workspaceFilters.value, [key]: value };
  selectedDetectionIndex.value = 0;
}

function setGalleryFilter(key, value) {
  galleryFilters.value = { ...galleryFilters.value, [key]: value };
}

function setPreviewScale(value) {
  previewScale.value = value;
}

async function saveExperimentRun() {
  if (!detection.value) {
    setError("请先完成一次识别后再保存实验。");
    return;
  }
  try {
    error.value = "";
    const saved = await createExperiment({
      imageId: imageId.value || null,
      modelVersion: detection.value.modelVersion,
      imgsz: detectOptions.value.imgsz,
      conf: detectOptions.value.conf,
      iou: detectOptions.value.iou,
      latencyMs: detection.value.latencyMs,
      boxCount: detectionList.value.length,
      avgConfidence: Number(stats.value.avgConfidence || 0),
      topClass: selectedDetection.value?.className || detectionSummary.value.byCategory[0]?.className || null,
      minConfidenceFilter: workspaceFilters.value.minConfidence,
      classKeywordFilter: workspaceFilters.value.classKeyword,
      note: experimentNote.value
    });
    experimentRuns.value = [saved, ...experimentRuns.value].slice(0, 50);
    experimentNote.value = "";
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "实验记录保存失败。");
  }
}

async function clearExperimentRuns() {
  try {
    await Promise.all(experimentRuns.value.map((item) => deleteExperiment(item.id)));
    experimentRuns.value = [];
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "清空实验记录失败。");
  }
}

async function updateGalleryItem(imageId, payload) {
  try {
    const updated = await updateImage(imageId, payload);
    galleryItems.value = galleryItems.value.map((item) => item.imageId === imageId ? { ...item, ...updated, imageUrl: withToken(updated.imageUrl) } : item);
    historyItems.value = historyItems.value.map((item) => item.imageId === imageId ? { ...item, ...updated, imageUrl: withToken(updated.imageUrl) } : item);
    if (viewerItem.value?.imageId === imageId) {
      viewerItem.value = { ...viewerItem.value, ...updated, imageUrl: withToken(updated.imageUrl) };
    }
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "图片状态更新失败。");
  }
}

async function toggleFavorite(imageId, nextValue) {
  await updateGalleryItem(imageId, { favorite: nextValue });
}

async function toggleFlagged(imageId, nextValue, reviewNote = undefined) {
  await updateGalleryItem(imageId, { flagged: nextValue, reviewNote });
}

async function updateReviewMeta(imageId, payload) {
  await updateGalleryItem(imageId, payload);
}

async function uploadModelAction(file) {
  if (!file) {
    setError("请先选择模型文件。");
    return false;
  }
  modelUploadLoading.value = true;
  error.value = "";
  try {
    currentModelInfo.value = await uploadModel(file);
    await refreshWorkspaceData();
    return true;
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "模型上传失败。");
    return false;
  } finally {
    modelUploadLoading.value = false;
  }
}

async function useModelAction(modelPath) {
  modelUploadLoading.value = true;
  error.value = "";
  try {
    currentModelInfo.value = await useModel(modelPath);
    await refreshWorkspaceData();
    return true;
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "模型切换失败。");
    return false;
  } finally {
    modelUploadLoading.value = false;
  }
}

async function removeImageAction(imageId) {
  try {
    await deleteImage(imageId);
    galleryItems.value = galleryItems.value.filter((item) => item.imageId !== imageId);
    historyItems.value = historyItems.value.filter((item) => item.imageId !== imageId);
    if (viewerItem.value?.imageId === imageId) {
      viewerItem.value = null;
    }
  } catch (e) {
    setError(e?.response?.data?.message || e.message || "删除图片失败。");
  }
}

export function useAppStore() {
  return {
    selectedFile,
    previewUrl,
    imageId,
    detection,
    detectionList,
    selectedDetection,
    sessionId,
    chatMessages,
    historyItems,
    galleryItems,
    galleryCards,
    question,
    extraContext,
    loading,
    chatLoading,
    error,
    currentUser,
    authLoading,
    bootstrapped,
    selectedDetectionIndex,
    viewerItem,
    detectOptions,
    workspaceFilters,
    galleryFilters,
    previewScale,
    experimentRuns,
    experimentNote,
    isAuthenticated,
    stats,
    classSummary,
    historySummary,
    overviewStats,
    currentModelInfo,
    availableModels,
    modelUploadLoading,
    flaggedItems,
    favoriteItems,
    overlayBoxes,
    dashboardCards,
    detectionSummary,
    selectedDetectionMeta,
    formatPercent,
    formatDate,
    bootstrap,
    loginAction,
    registerAction,
    logoutAction,
    refreshWorkspaceData,
    setFile,
    setPreviewMetrics,
    uploadAndDetectAction,
    rerunDetectionAction,
    generateAdviceAction,
    askFollowupAction,
    selectDetection,
    openViewer,
    closeViewer,
    reopenFromGallery,
    setDetectOption,
    setWorkspaceFilter,
    setGalleryFilter,
    setPreviewScale,
    saveExperimentRun,
    clearExperimentRuns,
    toggleFavorite,
    toggleFlagged,
    updateReviewMeta,
    uploadModelAction,
    useModelAction,
    removeImageAction,
    resetWorkspaceFilters,
    withToken,
    setError
  };
}
