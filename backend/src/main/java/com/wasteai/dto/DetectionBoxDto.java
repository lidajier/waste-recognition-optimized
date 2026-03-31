package com.wasteai.dto;

public record DetectionBoxDto(
        String className,
        float confidence,
        float x1,
        float y1,
        float x2,
        float y2
) {
}
