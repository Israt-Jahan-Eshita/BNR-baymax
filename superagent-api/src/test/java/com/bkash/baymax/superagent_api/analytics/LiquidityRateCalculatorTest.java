package com.bkash.baymax.superagent_api.analytics;

import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.LiquidityResourceType;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiquidityRateCalculatorTest {

    private final LiquidityRateCalculator calculator =
            new LiquidityRateCalculator();

    @Test
    void physicalCashShouldTreatCashOutAsConsumption() {
        Instant now =
                Instant.parse("2026-07-11T10:00:00Z");

        List<SimulatedTransaction> transactions =
                List.of(
                        transaction(
                                TransactionType.CASH_OUT,
                                "3000.00",
                                now.minusSeconds(300)
                        ),
                        transaction(
                                TransactionType.CASH_IN,
                                "1500.00",
                                now.minusSeconds(600)
                        )
                );

        BigDecimal rate =
                calculator.calculateConsumptionRate(
                        transactions,
                        LiquidityResourceType.PHYSICAL_CASH,
                        now.minusSeconds(900),
                        15
                );

        assertEquals(
                new BigDecimal("100.0000"),
                rate
        );
    }

    @Test
    void providerEMoneyShouldTreatCashInAsConsumption() {
        Instant now =
                Instant.parse("2026-07-11T10:00:00Z");

        List<SimulatedTransaction> transactions =
                List.of(
                        transaction(
                                TransactionType.CASH_IN,
                                "3000.00",
                                now.minusSeconds(300)
                        ),
                        transaction(
                                TransactionType.CASH_OUT,
                                "1500.00",
                                now.minusSeconds(600)
                        )
                );

        BigDecimal rate =
                calculator.calculateConsumptionRate(
                        transactions,
                        LiquidityResourceType.PROVIDER_E_MONEY,
                        now.minusSeconds(900),
                        15
                );

        assertEquals(
                new BigDecimal("100.0000"),
                rate
        );
    }

    @Test
    void shouldCalculateWeightedConsumptionRate() {
        BigDecimal weightedRate =
                calculator.calculateWeightedRate(
                        new BigDecimal("900"),
                        new BigDecimal("600"),
                        new BigDecimal("400")
                );

        assertEquals(
                new BigDecimal("710.0000"),
                weightedRate
        );
    }

    private SimulatedTransaction transaction(
            TransactionType transactionType,
            String amount,
            Instant occurredAt
    ) {
        return SimulatedTransaction.builder()
                .transactionType(transactionType)
                .amount(new BigDecimal(amount))
                .occurredAt(occurredAt)
                .build();
    }
}
