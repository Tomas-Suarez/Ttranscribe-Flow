package com.dev.transcribeflow.transcription;

import com.dev.transcribeflow.core.config.AiEngineConfig;
import com.dev.transcribeflow.core.exception.AiEngineException;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.settings.Language;
import com.dev.transcribeflow.transcription.dto.TranscriptionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class TranscriptionService {

    private final AiEngineConfig aiEngineConfig;
    private final WebClient.Builder webClientBuilder;
    private final MessageUtils messageUtils;

    public String transcribe(MultipartFile audioFile, Language lang){
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", audioFile.getResource())
                .filename(audioFile.getOriginalFilename());

        TranscriptionResponseDTO response = webClientBuilder.build()
                .post()
                .uri(aiEngineConfig.getBaseUrl() + "/transcribe?language=" + lang.getShortCode())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(TranscriptionResponseDTO.class)
                .block();

        if (response == null || !"success".equals(response.status())) {
            throw new AiEngineException(messageUtils.getMessage(
                    "ai.engine.error"));
        }

        return response.transcription();
    }
}
