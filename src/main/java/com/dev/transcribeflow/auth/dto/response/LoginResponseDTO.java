package com.dev.transcribeflow.auth.dto.response;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String email,
        String message
) {
}
