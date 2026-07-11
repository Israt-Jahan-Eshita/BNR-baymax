package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.ProviderDataHealthResponse;
import com.bkash.baymax.superagent_api.service.ProviderDataHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data-health")
@RequiredArgsConstructor
public class ProviderDataHealthController {

    private final ProviderDataHealthService
            providerDataHealthService;

    @GetMapping
    public List<ProviderDataHealthResponse> getDataHealth(
            @RequestParam
            String agentCode
    ) {
        return providerDataHealthService
                .getDataHealth(agentCode);
    }
}
