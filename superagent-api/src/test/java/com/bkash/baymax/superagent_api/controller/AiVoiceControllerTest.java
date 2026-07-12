package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.service.ai.OpenAiAudioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiVoiceControllerTest {

    @Mock
    private OpenAiAudioService openAiAudioService;

    @InjectMocks
    private AiVoiceController aiVoiceController;

    @Test
    void transcribe_success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.wav", "audio/wav", "dummy".getBytes());
        when(openAiAudioService.transcribe(file)).thenReturn("Hello");

        ResponseEntity<Map<String, String>> response = aiVoiceController.transcribe(file);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Hello", response.getBody().get("text"));
    }

    @Test
    void transcribe_failure() {
        MockMultipartFile file = new MockMultipartFile("file", "test.wav", "audio/wav", "dummy".getBytes());
        when(openAiAudioService.transcribe(file)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = aiVoiceController.transcribe(file);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Transcription failed", response.getBody().get("error"));
    }

    @Test
    void generateSpeech_success() {
        when(openAiAudioService.generateSpeech("Hello")).thenReturn("audioData".getBytes());

        ResponseEntity<byte[]> response = aiVoiceController.generateSpeech(Map.of("text", "Hello"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("audioData", new String(response.getBody()));
        assertEquals("audio/mpeg", response.getHeaders().getContentType().toString());
    }

    @Test
    void generateSpeech_badRequest() {
        ResponseEntity<byte[]> response = aiVoiceController.generateSpeech(Map.of("text", ""));
        assertEquals(400, response.getStatusCode().value());

        ResponseEntity<byte[]> response2 = aiVoiceController.generateSpeech(Map.of());
        assertEquals(400, response2.getStatusCode().value());
    }

    @Test
    void generateSpeech_failure() {
        when(openAiAudioService.generateSpeech("Hello")).thenReturn(new byte[0]);

        ResponseEntity<byte[]> response = aiVoiceController.generateSpeech(Map.of("text", "Hello"));

        assertEquals(500, response.getStatusCode().value());
    }
}
