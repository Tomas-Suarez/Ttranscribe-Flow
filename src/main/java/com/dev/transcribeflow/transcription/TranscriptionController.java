package com.dev.transcribeflow.transcription;

import com.dev.transcribeflow.settings.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/transcription")
@RequiredArgsConstructor
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    @PostMapping("/upload")
    public ResponseEntity<String> handleAudioUpload(
            @RequestParam("file")MultipartFile file,
            @RequestParam("lang")String langIso){

        Language language = Language.fromIsoCode(langIso);

        String transcription = transcriptionService.transcribe(file, language);

        return ResponseEntity.ok(transcription);
    }

}
