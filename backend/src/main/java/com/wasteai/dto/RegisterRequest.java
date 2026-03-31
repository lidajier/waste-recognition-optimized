package com.wasteai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 32) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 64) String displayName,
        @NotBlank @Size(min = 6, max = 64) String password
) {
}
