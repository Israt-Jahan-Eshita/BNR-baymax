package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.request.AiChatRequest;
import com.bkash.baymax.superagent_api.dto.response.AiChatResponse;
import com.bkash.baymax.superagent_api.dto.response.BaymaxResponse;
import com.bkash.baymax.superagent_api.service.ai.BaymaxReasoningService;
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

    private final BaymaxReasoningService baymaxReasoningService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        BaymaxResponse baymaxResponse = baymaxReasoningService.analyze(
                request.agentCode(), 
                request.question(),
                request.language(),
                request.persona()
        );
        return ResponseEntity.ok(new AiChatResponse(baymaxResponse.answer()));
    }

    @PostMapping("/analyze")
    public ResponseEntity<BaymaxResponse> analyze(@Valid @RequestBody AiChatRequest request) {
        BaymaxResponse baymaxResponse = baymaxReasoningService.analyze(
                request.agentCode(), 
                request.question(),
                request.language(),
                request.persona()
        );
        return ResponseEntity.ok(baymaxResponse);
    }
}
