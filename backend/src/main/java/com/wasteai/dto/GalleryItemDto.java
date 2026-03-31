package com.wasteai.dto;

import java.time.LocalDateTime;

public record GalleryItemDto(
        String imageId,
        String imageName,
        String imageUrl,
        Boolean favorite,
        Boolean flagged,
        String reviewNote,
        String detectionId,
        String sessionId,
        String modelVersion,
        Long latencyMs,
        Integer boxCount,
        String topClass,
        Float topConfidence,
        String advicePreview,
        LocalDateTime uploadedAt
) {
}
