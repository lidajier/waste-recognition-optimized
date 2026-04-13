package com.wasteai.dto;

public record ModelUploadResponse(
        String modelPath,
        String modelVersion
) {
}
