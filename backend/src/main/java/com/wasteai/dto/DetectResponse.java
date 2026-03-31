package com.wasteai.dto;

import java.util.List;

public record DetectResponse(
        String detectionId,
        String imageId,
        String modelVersion,
        long latencyMs,
        List<DetectionBoxDto> detections
) {
}
