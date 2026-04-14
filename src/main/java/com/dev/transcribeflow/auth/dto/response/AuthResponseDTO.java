package com.dev.transcribeflow.auth.dto.response;

import java.util.UUID;

public record AuthResponseDTO(
        UUID id,
        String username,
        String email,
        String role,
        String message
){
}
