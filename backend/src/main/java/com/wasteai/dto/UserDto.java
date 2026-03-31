package com.wasteai.dto;

import java.time.LocalDateTime;

public record UserDto(
        String id,
        String username,
        String email,
        String displayName,
        String role,
        LocalDateTime createdAt
) {
}
