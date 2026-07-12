package com.bkash.baymax.superagent_api.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatRequest(
        String model,
        List<Message> messages,
        double temperature,
        ResponseFormat response_format
) {
    public record Message(
            String role,
            String content
    ) {}

    public record ResponseFormat(
            String type
    ) {}
}
