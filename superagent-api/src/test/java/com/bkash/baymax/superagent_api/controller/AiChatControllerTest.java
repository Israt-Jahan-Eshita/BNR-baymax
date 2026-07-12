package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.request.AiChatRequest;
import com.bkash.baymax.superagent_api.dto.response.AiChatResponse;
import com.bkash.baymax.superagent_api.dto.response.BaymaxResponse;
import com.bkash.baymax.superagent_api.service.ai.BaymaxReasoningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    @Mock
    private BaymaxReasoningService baymaxReasoningService;

    @InjectMocks
    private AiChatController aiChatController;

    @Test
    void chat_success() {
        AiChatRequest request = new AiChatRequest("A100", "Hello");
        BaymaxResponse baymaxResponse = new BaymaxResponse("Response", "HIGH", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        when(baymaxReasoningService.analyze(anyString(), anyString())).thenReturn(baymaxResponse);

        ResponseEntity<AiChatResponse> responseEntity = aiChatController.chat(request);
        
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("Response", responseEntity.getBody().answer());
    }

    @Test
    void analyze_success() {
        AiChatRequest request = new AiChatRequest("A100", "Hello");
        BaymaxResponse baymaxResponse = new BaymaxResponse("Response", "HIGH", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        when(baymaxReasoningService.analyze(anyString(), anyString())).thenReturn(baymaxResponse);

        ResponseEntity<BaymaxResponse> responseEntity = aiChatController.analyze(request);
        
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals("Response", responseEntity.getBody().answer());
        assertEquals("HIGH", responseEntity.getBody().confidence());
    }
}
