package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.ValidationMetricsResponse;
import com.bkash.baymax.superagent_api.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @GetMapping("/metrics")
    public ValidationMetricsResponse getValidationMetrics() {
        return validationService.getValidationMetrics();
    }
}
