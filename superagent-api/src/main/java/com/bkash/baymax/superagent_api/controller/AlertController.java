package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.AlertDetailResponse;
import com.bkash.baymax.superagent_api.dto.response.AlertPageResponse;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.service.AlertQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertQueryService alertQueryService;

    @GetMapping
    public AlertPageResponse getAlerts(
            @RequestParam String agentCode,
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) AlertSeverity severity,
            Pageable pageable
    ) {
        return alertQueryService.getAlerts(
                agentCode,
                providerCode,
                alertType,
                severity,
                pageable
        );
    }

    @GetMapping("/{alertCode}")
    public AlertDetailResponse getAlert(
            @PathVariable String alertCode
    ) {
        return alertQueryService.getAlert(alertCode);
    }
}
