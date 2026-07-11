package com.bkash.baymax.superagent_api.dto.request;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<Message> messages,
        double temperature
) {
    public record Message(
            String role,
            String content
    ) {}
}
