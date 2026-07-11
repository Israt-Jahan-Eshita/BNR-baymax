package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.AgentBalanceResponse;
import com.bkash.baymax.superagent_api.service.BalanceQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceQueryService balanceQueryService;

    @GetMapping
    public AgentBalanceResponse getBalances(
            @RequestParam
            String agentCode
    ) {
        return balanceQueryService.getAgentBalances(agentCode);
    }
}
