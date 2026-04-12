package com.wasteai.dto;

import jakarta.validation.constraints.NotNull;

public record ExperimentRunRequest(
        String imageId,
        String modelVersion,
        @NotNull Integer imgsz,
        @NotNull Float conf,
        @NotNull Float iou,
        @NotNull Long latencyMs,
        @NotNull Integer boxCount,
        @NotNull Double avgConfidence,
        String topClass,
        Float minConfidenceFilter,
        String classKeywordFilter,
        String note
) {
}
