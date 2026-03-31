package com.wasteai.dto;

import jakarta.validation.constraints.NotBlank;

public record AdviceRequest(@NotBlank String detectionId, String extraContext) {
}
