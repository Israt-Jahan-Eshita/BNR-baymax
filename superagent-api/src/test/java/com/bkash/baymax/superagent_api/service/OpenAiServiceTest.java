package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.OpenAiChatRequest;
import com.bkash.baymax.superagent_api.dto.response.OpenAiChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceTest {

    @Mock
    private RestClient restClient;
    
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private OpenAiService openAiService;

    @BeforeEach
    void setUp() {
        // Setup default mock behavior for RestClient chain
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(OpenAiChatRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getChatCompletion_success() {
        OpenAiChatResponse mockResponse = new OpenAiChatResponse(
                List.of(new OpenAiChatResponse.Choice(
                        new OpenAiChatResponse.Message("Hello world")
                ))
        );

        when(responseSpec.body(OpenAiChatResponse.class)).thenReturn(mockResponse);

        String result = openAiService.getChatCompletion("System", "User");
        assertEquals("Hello world", result);
    }

    @Test
    void getStructuredChatCompletion_success() {
        OpenAiChatResponse mockResponse = new OpenAiChatResponse(
                List.of(new OpenAiChatResponse.Choice(
                        new OpenAiChatResponse.Message("{\"answer\":\"Yes\"}")
                ))
        );

        when(responseSpec.body(OpenAiChatResponse.class)).thenReturn(mockResponse);

        String result = openAiService.getStructuredChatCompletion("System", "User");
        assertEquals("{\"answer\":\"Yes\"}", result);
    }

    @Test
    void getChatCompletion_nullResponse() {
        when(responseSpec.body(OpenAiChatResponse.class)).thenReturn(null);

        String result = openAiService.getChatCompletion("System", "User");
        assertNull(result);
    }

    @Test
    void getChatCompletion_exception() {
        when(responseSpec.body(OpenAiChatResponse.class)).thenThrow(new RuntimeException("API error"));

        String result = openAiService.getChatCompletion("System", "User");
        assertNull(result);
    }
}
