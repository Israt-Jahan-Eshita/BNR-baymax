package com.bkash.baymax.superagent_api.service.ai;

import com.bkash.baymax.superagent_api.dto.ai.BaymaxOperationalContext;
import com.bkash.baymax.superagent_api.dto.response.BaymaxResponse;
import com.bkash.baymax.superagent_api.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaymaxReasoningServiceTest {

    @Mock
    private BaymaxOperationalContextService contextService;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BaymaxReasoningService reasoningService;

    @Test
    void analyze_success() throws Exception {
        String agentCode = "A100";
        String question = "What is my status?";

        BaymaxOperationalContext context = org.mockito.Mockito.mock(BaymaxOperationalContext.class);

        when(contextService.buildContext(agentCode)).thenReturn(context);
        when(objectMapper.writeValueAsString(context)).thenReturn("{}");

        String mockResponseJson = "{\"answer\":\"OK\",\"confidence\":\"HIGH\",\"reasoningSteps\":[],\"evidenceList\":[],\"actionItems\":[],\"whatIfProjections\":[]}";
        when(openAiService.getStructuredChatCompletion(anyString(), eq(question))).thenReturn(mockResponseJson);

        BaymaxResponse expectedResponse = new BaymaxResponse("OK", "HIGH", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(objectMapper.readValue(mockResponseJson, BaymaxResponse.class)).thenReturn(expectedResponse);

        BaymaxResponse response = reasoningService.analyze(agentCode, question, "English", "Professional Assistant");

        assertNotNull(response);
        assertEquals("OK", response.answer());
        assertEquals("HIGH", response.confidence());
    }

    @Test
    void analyze_fallbackOnEmptyResponse() throws Exception {
        String agentCode = "A100";
        String question = "Status?";

        when(contextService.buildContext(agentCode)).thenReturn(org.mockito.Mockito.mock(BaymaxOperationalContext.class));
        when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any())).thenReturn("{}");
        
        when(openAiService.getStructuredChatCompletion(anyString(), eq(question))).thenReturn("");

        BaymaxResponse response = reasoningService.analyze(agentCode, question, "English", "Professional Assistant");

        assertNotNull(response);
        assertEquals("I am currently unable to process your request (Empty response).", response.answer());
        assertEquals("LOW", response.confidence());
    }

    @Test
    void analyze_fallbackOnException() throws Exception {
        String agentCode = "A100";
        String question = "Status?";

        when(contextService.buildContext(agentCode)).thenThrow(new RuntimeException("DB Error"));

        BaymaxResponse response = reasoningService.analyze(agentCode, question, "English", "Professional Assistant");

        assertNotNull(response);
        assertTrue(response.answer().contains("Failed to analyze system state. Error: DB Error"));
        assertEquals("LOW", response.confidence());
    }
}
