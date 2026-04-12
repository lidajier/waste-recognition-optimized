const CATEGORY_RULES = [
  {
    match: ["plastic", "bottle", "cup", "paper", "cardboard", "can", "metal", "glass"],
    category: "可回收物",
    color: "#22c55e",
    tips: "建议保持相对洁净、干燥后投放；纸箱类尽量压扁。"
  },
  {
    match: ["battery", "drug", "paint", "lamp"],
    category: "有害垃圾",
    color: "#ef4444",
    tips: "避免破损或泄漏，单独投放到有害垃圾回收点。"
  },
  {
    match: ["food", "banana", "bone", "vegetable"],
    category: "厨余垃圾",
    color: "#f59e0b",
    tips: "尽量沥干水分后投放，避免与塑料包装混投。"
  }
];

export function classifyWaste(className = "") {
  const value = className.toLowerCase();
  const matched = CATEGORY_RULES.find((rule) => rule.match.some((item) => value.includes(item)));
  if (matched) {
    return matched;
  }
  return {
    category: "其他垃圾",
    color: "#64748b",
    tips: "若材质或污染程度难以判断，建议按其他垃圾投放并结合当地标准确认。"
  };
}

export function confidenceLevel(confidence = 0) {
  if (confidence >= 0.8) return "高";
  if (confidence >= 0.5) return "中";
  return "低";
}

export function confidenceHint(confidence = 0) {
  if (confidence >= 0.8) return "结果较稳定，可直接参考。";
  if (confidence >= 0.5) return "结果可作为参考，建议结合图片细节确认。";
  return "结果可信度较低，建议更换角度或补充清晰图片。";
}
