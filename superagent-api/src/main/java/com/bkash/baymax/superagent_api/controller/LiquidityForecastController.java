package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.LiquidityForecastResponse;
import com.bkash.baymax.superagent_api.service.LiquidityForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/liquidity")
@RequiredArgsConstructor
public class LiquidityForecastController {

    private final LiquidityForecastService liquidityForecastService;

    @GetMapping("/forecast")
    public LiquidityForecastResponse getForecast(
            @RequestParam
            String agentCode
    ) {
        return liquidityForecastService
                .getForecast(agentCode);
    }
}
