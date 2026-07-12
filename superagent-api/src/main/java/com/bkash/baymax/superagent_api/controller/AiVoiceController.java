package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.service.ai.OpenAiAudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiVoiceController {

    private final OpenAiAudioService openAiAudioService;

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> transcribe(@RequestParam("file") MultipartFile file) {
        String text = openAiAudioService.transcribe(file);
        if (text == null) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Transcription failed"));
        }
        return ResponseEntity.ok(Map.of("text", text));
    }

    @PostMapping("/speech")
    public ResponseEntity<byte[]> generateSpeech(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] audio = openAiAudioService.generateSpeech(text);
        if (audio == null || audio.length == 0) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        return ResponseEntity.ok().headers(headers).body(audio);
    }
}
