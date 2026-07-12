package com.bkash.baymax.superagent_api.service.ai;

import com.bkash.baymax.superagent_api.dto.ai.BaymaxOperationalContext.WhatIfProjectionContext;
import com.bkash.baymax.superagent_api.dto.response.LiquidityResourceForecastResponse;
import com.bkash.baymax.superagent_api.model.enums.ForecastConfidence;
import com.bkash.baymax.superagent_api.model.enums.LiquidityPressureStatus;
import com.bkash.baymax.superagent_api.model.enums.LiquidityResourceType;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaymaxWhatIfServiceTest {

    private final BaymaxWhatIfService whatIfService = new BaymaxWhatIfService();

    @Test
    void generateProjections_withValidData_generatesCorrectRunways() {
        LiquidityResourceForecastResponse resource = new LiquidityResourceForecastResponse(
                LiquidityResourceType.PROVIDER_E_MONEY,
                "BKASH",
                "bKash E-Money",
                new BigDecimal("10000"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                null,
                LiquidityPressureStatus.STABLE,
                ForecastConfidence.HIGH,
                100,
                10,
                ProviderDataHealthStatus.LIVE,
                null,
                List.of()
        );

        List<WhatIfProjectionContext> projections = whatIfService.generateProjections(List.of(resource));

        assertEquals(1, projections.size());
        WhatIfProjectionContext proj = projections.get(0);
        assertEquals("bKash E-Money", proj.resourceName());
        
        // baseline: 10000 / 100 = 100
        assertEquals(100, proj.baselineRunwayMinutes());
        // 20% spike: 10000 / 120 = 83
        assertEquals(83, proj.runwayWith20PercentDemandSpike());
        // 50% spike: 10000 / 150 = 67
        assertEquals(67, proj.runwayWith50PercentDemandSpike());
        // 100% spike: 10000 / 200 = 50
        assertEquals(50, proj.runwayWith100PercentDemandSpike());
    }

    @Test
    void generateProjections_withZeroRate_returnsEmpty() {
        LiquidityResourceForecastResponse resource = new LiquidityResourceForecastResponse(
                LiquidityResourceType.PROVIDER_E_MONEY,
                "BKASH",
                "bKash E-Money",
                new BigDecimal("10000"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                null,
                null,
                LiquidityPressureStatus.STABLE,
                ForecastConfidence.HIGH,
                100,
                10,
                ProviderDataHealthStatus.LIVE,
                null,
                List.of()
        );

        List<WhatIfProjectionContext> projections = whatIfService.generateProjections(List.of(resource));

        assertTrue(projections.isEmpty());
    }
}
