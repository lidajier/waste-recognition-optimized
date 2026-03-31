package com.wasteai.dto;

public record ImageUpdateRequest(
        Boolean favorite,
        Boolean flagged,
        String reviewNote
) {
}
