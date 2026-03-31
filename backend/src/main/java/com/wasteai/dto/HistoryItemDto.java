package com.wasteai.dto;

import java.time.LocalDateTime;

public record HistoryItemDto(
        String sessionId,
        String imageId,
        String imageName,
        String imageUrl,
        Boolean favorite,
        Boolean flagged,
        String detectionId,
        String modelVersion,
        Long latencyMs,
        Integer boxCount,
        String topClass,
        Float topConfidence,
        String advicePreview,
        LocalDateTime createdAt
) {
}
