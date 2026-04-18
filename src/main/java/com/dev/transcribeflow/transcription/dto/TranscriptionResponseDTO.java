package com.dev.transcribeflow.transcription.dto;

import com.dev.transcribeflow.settings.Language;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TranscriptionResponseDTO(
        String status,

        @JsonProperty("language_used")
        String languageUsed,

        String transcription
) {
}
