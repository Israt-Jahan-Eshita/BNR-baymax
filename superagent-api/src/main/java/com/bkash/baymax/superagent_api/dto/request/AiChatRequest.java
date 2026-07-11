package com.bkash.baymax.superagent_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "Question is required")
        String question,
        
        @NotBlank(message = "Agent code is required")
        String agentCode
) {
}
