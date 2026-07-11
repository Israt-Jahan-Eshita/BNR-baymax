package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.DashboardAggregateResponse;
import com.bkash.baymax.superagent_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/aggregate")
    public DashboardAggregateResponse getDashboardAggregate(
            @RequestParam String agentCode
    ) {
        return dashboardService.getDashboardAggregate(agentCode);
    }
}
