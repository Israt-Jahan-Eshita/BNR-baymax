package com.bkash.baymax.superagent_api.service.ai;

import com.bkash.baymax.superagent_api.dto.ai.BaymaxOperationalContext;
import com.bkash.baymax.superagent_api.dto.response.BaymaxResponse;
import com.bkash.baymax.superagent_api.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaymaxReasoningService {

    private final BaymaxOperationalContextService contextService;
    private final OpenAiService openAiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public BaymaxResponse analyze(String agentCode, String question, String language, String persona) {
        try {
            BaymaxOperationalContext context = contextService.buildContext(agentCode);
            String contextJson = objectMapper.writeValueAsString(context);

            String systemPrompt = "You are BNR Baymax, an intelligent decision-support AI for bKash, Nagad, and Rocket super-agents in Bangladesh. "
                    + "Your persona is: " + persona + ". You must reply strictly in this language: " + language + ".\n"
                    + "The user will ask you a question. You must answer based ONLY on the following real-time JSON context of their operational state.\n"
                    + "If the answer is not in the context, say you don't know.\n"
                    + "The context includes 'whatIfProjections' for 20%, 50%, and 100% demand spikes. If the user asks a hypothetical or projection question, interpret these numbers and place your insights in the 'whatIfProjections' JSON array.\n"
                    + "You MUST return your response as a valid JSON object matching the following structure:\n"
                    + "{\n"
                    + "  \"answer\": \"Your concise answer to the question\",\n"
                    + "  \"confidence\": \"HIGH, MEDIUM, or LOW\",\n"
                    + "  \"reasoningSteps\": [\"Step 1 of reasoning\", \"Step 2\"],\n"
                    + "  \"evidenceList\": [\"Fact 1 from context\", \"Fact 2 from context\"],\n"
                    + "  \"actionItems\": [\"Actionable recommendation 1\"],\n"
                    + "  \"whatIfProjections\": [\"Projection insight 1\", \"Projection insight 2\"]\n"
                    + "}\n\n"
                    + "Context:\n" + contextJson;

            String jsonResult = openAiService.getStructuredChatCompletion(systemPrompt, question);
            
            if (jsonResult == null || jsonResult.trim().isEmpty()) {
                return buildFallbackResponse("I am currently unable to process your request (Empty response).");
            }
            
            return objectMapper.readValue(jsonResult, BaymaxResponse.class);
        } catch (Exception e) {
            log.error("Failed to analyze system state", e);
            return buildFallbackResponse("Failed to analyze system state. Error: " + e.getMessage());
        }
    }

    private BaymaxResponse buildFallbackResponse(String message) {
        return new BaymaxResponse(
                message,
                "LOW",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}
