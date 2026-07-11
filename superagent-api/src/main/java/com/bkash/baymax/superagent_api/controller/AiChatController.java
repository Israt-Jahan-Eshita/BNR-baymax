package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.request.AiChatRequest;
import com.bkash.baymax.superagent_api.dto.response.AiChatResponse;
import com.bkash.baymax.superagent_api.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        String answer = aiChatService.chat(request.agentCode(), request.question());
        return ResponseEntity.ok(new AiChatResponse(answer));
    }
}
