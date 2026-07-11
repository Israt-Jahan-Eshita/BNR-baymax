package com.bkash.baymax.superagent_api.analytics;

import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.LiquidityResourceType;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Component
public class LiquidityRateCalculator {

    private static final BigDecimal FIFTEEN_MINUTE_WEIGHT =
            new BigDecimal("0.50");

    private static final BigDecimal THIRTY_MINUTE_WEIGHT =
            new BigDecimal("0.30");

    private static final BigDecimal SIXTY_MINUTE_WEIGHT =
            new BigDecimal("0.20");

    private static final int RATE_SCALE = 4;

    public BigDecimal calculateConsumptionRate(
            List<SimulatedTransaction> transactions,
            LiquidityResourceType resourceType,
            Instant from,
            int windowMinutes
    ) {
        BigDecimal netConsumption = transactions.stream()
                .filter(transaction ->
                        !transaction.getOccurredAt().isBefore(from)
                )
                .map(transaction ->
                        consumptionContribution(
                                transaction,
                                resourceType
                        )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return netConsumption.divide(
                BigDecimal.valueOf(windowMinutes),
                RATE_SCALE,
                RoundingMode.HALF_UP
        );
    }

    public BigDecimal calculateWeightedRate(
            BigDecimal rate15,
            BigDecimal rate30,
            BigDecimal rate60
    ) {
        return rate15
                .multiply(FIFTEEN_MINUTE_WEIGHT)
                .add(
                        rate30.multiply(
                                THIRTY_MINUTE_WEIGHT
                        )
                )
                .add(
                        rate60.multiply(
                                SIXTY_MINUTE_WEIGHT
                        )
                )
                .setScale(
                        RATE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal consumptionContribution(
            SimulatedTransaction transaction,
            LiquidityResourceType resourceType
    ) {
        return switch (resourceType) {
            case PHYSICAL_CASH ->
                    physicalCashContribution(transaction);

            case PROVIDER_E_MONEY ->
                    providerEMoneyContribution(transaction);
        };
    }

    private BigDecimal physicalCashContribution(
            SimulatedTransaction transaction
    ) {
        if (
                transaction.getTransactionType()
                        == TransactionType.CASH_OUT
        ) {
            return transaction.getAmount();
        }

        return transaction.getAmount().negate();
    }

    private BigDecimal providerEMoneyContribution(
            SimulatedTransaction transaction
    ) {
        if (
                transaction.getTransactionType()
                        == TransactionType.CASH_IN
        ) {
            return transaction.getAmount();
        }

        return transaction.getAmount().negate();
    }
}
