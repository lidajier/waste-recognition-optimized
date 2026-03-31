package com.wasteai.dto;

import jakarta.validation.constraints.NotBlank;

public record DetectRequest(
        @NotBlank String imageId,
        Integer imgsz,
        Float conf,
        Float iou
) {
}
