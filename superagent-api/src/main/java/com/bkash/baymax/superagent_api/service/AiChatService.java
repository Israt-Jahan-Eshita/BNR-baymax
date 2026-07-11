package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.DashboardAggregateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final DashboardService dashboardService;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String agentCode, String question) {
        try {
            DashboardAggregateResponse dashboardState = dashboardService.getDashboardAggregate(agentCode);
            String dashboardJson = objectMapper.writeValueAsString(dashboardState);

            String systemPrompt = "You are BNR Baymax, an intelligent decision-support AI for bKash, Nagad, and Rocket super-agents in Bangladesh. "
                    + "The user will ask you a question in English or Bangla. You must answer concisely based ONLY on the following real-time JSON context of their operational state. "
                    + "If the answer is not in the context, say you don't know.\n\nContext:\n" + dashboardJson;

            String answer = openAiService.getChatCompletion(systemPrompt, question);
            return answer != null ? answer : "I am currently unable to process your request.";
        } catch (Exception e) {
            return "Failed to analyze system state. Error: " + e.getMessage();
        }
    }
}
