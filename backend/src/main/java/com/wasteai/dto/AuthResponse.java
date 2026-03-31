package com.wasteai.dto;

public record AuthResponse(
        String token,
        UserDto user
) {
}
