package com.bkash.baymax.superagent_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "Question is required")
        String question,
        
        @NotBlank(message = "Agent code is required")
        String agentCode,
        
        String language,
        String persona
) {
    public String language() {
        return language == null || language.isBlank() ? "English" : language;
    }
    
    public String persona() {
        return persona == null || persona.isBlank() ? "Professional Assistant" : persona;
    }
}
