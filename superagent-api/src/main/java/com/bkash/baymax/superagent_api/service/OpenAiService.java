package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.OpenAiChatRequest;
import com.bkash.baymax.superagent_api.dto.response.OpenAiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final RestClient openAiRestClient;

    public String getChatCompletion(String systemPrompt, String userPrompt) {
        try {
            OpenAiChatRequest request = new OpenAiChatRequest(
                    "gpt-4o-mini", // Use gpt-4o-mini for speed and low cost
                    List.of(
                            new OpenAiChatRequest.Message("system", systemPrompt),
                            new OpenAiChatRequest.Message("user", userPrompt)
                    ),
                    0.2
            );

            OpenAiChatResponse response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content().trim();
            }
        } catch (Exception e) {
            log.error("Failed to get response from OpenAI", e);
        }
        return null;
    }
}
