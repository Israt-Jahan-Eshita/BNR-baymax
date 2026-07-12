package com.bkash.baymax.superagent_api.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiAudioService {

    private final RestClient openAiRestClient;

    public String transcribe(MultipartFile audioFile) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", audioFile.getResource());
            body.add("model", "whisper-1");

            Map response = openAiRestClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("text")) {
                return (String) response.get("text");
            }
        } catch (Exception e) {
            log.error("Failed to transcribe audio", e);
        }
        return null;
    }

    public byte[] generateSpeech(String text) {
        try {
            Map<String, String> request = Map.of(
                    "model", "tts-1",
                    "input", text,
                    "voice", "alloy"
            );

            return openAiRestClient.post()
                    .uri("/audio/speech")
                    .body(request)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            log.error("Failed to generate speech", e);
        }
        return new byte[0];
    }
}
