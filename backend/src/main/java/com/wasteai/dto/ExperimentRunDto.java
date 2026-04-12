package com.wasteai.dto;

import java.time.LocalDateTime;

public record ExperimentRunDto(
        String id,
        String imageId,
        String imageUrl,
        String modelVersion,
        Integer imgsz,
        Float conf,
        Float iou,
        Long latencyMs,
        Integer boxCount,
        Double avgConfidence,
        String topClass,
        Float minConfidenceFilter,
        String classKeywordFilter,
        String note,
        LocalDateTime createdAt
) {
}
