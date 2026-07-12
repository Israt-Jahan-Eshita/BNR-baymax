package com.bkash.baymax.superagent_api.service.ai;

import com.bkash.baymax.superagent_api.dto.ai.BaymaxOperationalContext.WhatIfProjectionContext;
import com.bkash.baymax.superagent_api.dto.response.LiquidityResourceForecastResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class BaymaxWhatIfService {

    public List<WhatIfProjectionContext> generateProjections(List<LiquidityResourceForecastResponse> resources) {
        List<WhatIfProjectionContext> projections = new ArrayList<>();
        
        for (LiquidityResourceForecastResponse resource : resources) {
            BigDecimal rate = resource.weightedConsumptionPerMinute();
            BigDecimal balance = resource.currentBalance();
            
            if (rate != null && rate.signum() > 0 && balance != null) {
                Integer baseline = calculateRunway(balance, rate);
                Integer spike20 = calculateRunway(balance, rate.multiply(new BigDecimal("1.2")));
                Integer spike50 = calculateRunway(balance, rate.multiply(new BigDecimal("1.5")));
                Integer spike100 = calculateRunway(balance, rate.multiply(new BigDecimal("2.0")));
                
                projections.add(new WhatIfProjectionContext(
                    resource.resourceDisplayName(),
                    baseline,
                    spike20,
                    spike50,
                    spike100
                ));
            }
        }
        
        return projections;
    }
    
    private Integer calculateRunway(BigDecimal balance, BigDecimal rate) {
        if (rate == null || rate.signum() <= 0) return null;
        return balance.divide(rate, 0, RoundingMode.HALF_UP).intValue();
    }
}
