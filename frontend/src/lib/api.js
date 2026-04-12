import axios from "axios";

const client = axios.create({
  baseURL: "/api"
});

let authToken = localStorage.getItem("waste-ai-token") || "";

if (authToken) {
  client.defaults.headers.common.Authorization = `Bearer ${authToken}`;
}

client.interceptors.request.use((config) => {
  if (authToken) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${authToken}`;
  }
  return config;
});

export function setToken(token) {
  authToken = token || "";
  if (authToken) {
    localStorage.setItem("waste-ai-token", authToken);
    client.defaults.headers.common.Authorization = `Bearer ${authToken}`;
  } else {
    localStorage.removeItem("waste-ai-token");
    delete client.defaults.headers.common.Authorization;
  }
}

export function clearToken() {
  setToken("");
}

export function getToken() {
  return authToken;
}

export async function register(payload) {
  const { data } = await client.post("/auth/register", payload);
  return data;
}

export async function login(payload) {
  const { data } = await client.post("/auth/login", payload);
  return data;
}

export async function getMe() {
  const { data } = await client.get("/auth/me");
  return data;
}

export async function uploadImage(file) {
  const form = new FormData();
  form.append("file", file);
  const { data } = await client.post("/files/upload", form, {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: authToken ? `Bearer ${authToken}` : undefined
    }
  });
  return data;
}

export async function runDetection(imageId, options = {}) {
  const payload = {
    imageId,
    imgsz: options.imgsz ?? 512,
    conf: options.conf ?? 0.25,
    iou: options.iou ?? 0.7
  };
  const { data } = await client.post("/detect", payload);
  return data;
}

export async function generateAdvice(detectionId, extraContext = "") {
  const { data } = await client.post("/advice", { detectionId, extraContext });
  return data;
}

export async function sendChat(sessionId, message) {
  const { data } = await client.post(`/chat/${sessionId}`, { message });
  return data;
}

export async function getSession(sessionId) {
  const { data } = await client.get(`/sessions/${sessionId}`);
  return data;
}

export async function getHistory(limit = 10) {
  const { data } = await client.get("/history", { params: limit });
  return data;
}

export async function getGallery(limit = 12) {
  const { data } = await client.get("/gallery", { params: limit });
  return data;
}

export async function updateImage(imageId, payload) {
  const { data } = await client.patch(`/files/${imageId}`, payload);
  return data;
}

export async function deleteImage(imageId) {
  await client.delete(`/files/${imageId}`);
}

export async function getOverviewStats() {
  const { data } = await client.get("/stats/overview");
  return data;
}

export async function listExperiments() {
  const { data } = await client.get("/experiments");
  return data;
}

export async function createExperiment(payload) {
  const { data } = await client.post("/experiments", payload);
  return data;
}

export async function deleteExperiment(id) {
  await client.delete(`/experiments/${id}`);
}
