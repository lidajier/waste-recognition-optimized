package com.wasteai.dto;

import java.time.LocalDateTime;

public record GalleryItemDto(
        String imageId,
        String imageName,
        String imageUrl,
        Boolean favorite,
        Boolean flagged,
        String reviewNote,
        String reviewType,
        String correctedClass,
        String detectionId,
        String sessionId,
        String modelVersion,
        Long latencyMs,
        Integer boxCount,
        String topClass,
        Float topConfidence,
        String advicePreview,
        Integer imgsz,
        Float conf,
        Float iou,
        LocalDateTime uploadedAt
) {
}
