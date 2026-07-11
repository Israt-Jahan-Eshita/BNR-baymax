package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.analytics.LiquidityRateCalculator;
import com.bkash.baymax.superagent_api.dto.response.LiquidityForecastResponse;
import com.bkash.baymax.superagent_api.dto.response.LiquidityResourceForecastResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.ForecastConfidence;
import com.bkash.baymax.superagent_api.model.enums.LiquidityPressureStatus;
import com.bkash.baymax.superagent_api.model.enums.LiquidityResourceType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LiquidityForecastService {

    private static final BigDecimal HIGH_PRESSURE_MINUTES =
            new BigDecimal("60");

    private static final BigDecimal MEDIUM_PRESSURE_MINUTES =
            new BigDecimal("180");

    private static final int RUNWAY_SCALE = 2;

    private final AgentRepository agentRepository;
    private final PhysicalCashPositionRepository
            physicalCashPositionRepository;
    private final ProviderBalanceRepository
            providerBalanceRepository;
    private final SimulatedTransactionRepository
            simulatedTransactionRepository;
    private final LiquidityRateCalculator liquidityRateCalculator;
    private final Clock clock;

    @Transactional(readOnly = true)
    public LiquidityForecastResponse getForecast(
            String requestedAgentCode
    ) {
        String agentCode = normalizeAgentCode(
                requestedAgentCode
        );

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException(
                    "Agent not found: " + agentCode
            );
        }

        Instant now = Instant.now(clock);
        Instant sixtyMinutesAgo =
                now.minus(60, ChronoUnit.MINUTES);

        List<SimulatedTransaction> recentTransactions =
                simulatedTransactionRepository
                        .findAllByAgentAgentCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
                                agentCode,
                                sixtyMinutesAgo,
                                now
                        );

        PhysicalCashPosition physicalCashPosition =
                physicalCashPositionRepository
                        .findByAgentAgentCode(agentCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Physical cash position not found for agent "
                                                + agentCode
                                )
                        );

        List<LiquidityResourceForecastResponse> forecasts =
                new ArrayList<>();

        forecasts.add(
                buildPhysicalCashForecast(
                        physicalCashPosition,
                        recentTransactions,
                        now
                )
        );

        List<ProviderBalance> providerBalances =
                providerBalanceRepository
                        .findAllByAgentAgentCode(agentCode)
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        balance ->
                                                balance.getProvider()
                                                        .getProviderCode()
                                )
                        )
                        .toList();

        for (ProviderBalance providerBalance : providerBalances) {
            List<SimulatedTransaction> providerTransactions =
                    recentTransactions.stream()
                            .filter(transaction ->
                                    transaction.getProvider()
                                            .getProviderCode()
                                            .equals(
                                                    providerBalance
                                                            .getProvider()
                                                            .getProviderCode()
                                            )
                            )
                            .toList();

            forecasts.add(
                    buildProviderForecast(
                            providerBalance,
                            providerTransactions,
                            now
                    )
            );
        }

        return new LiquidityForecastResponse(
                agentCode,
                now,
                forecasts
        );
    }

    private LiquidityResourceForecastResponse
    buildPhysicalCashForecast(
            PhysicalCashPosition cashPosition,
            List<SimulatedTransaction> transactions,
            Instant now
    ) {
        return buildForecast(
                LiquidityResourceType.PHYSICAL_CASH,
                null,
                "Shared Physical Cash",
                cashPosition.getCashBalance(),
                transactions,
                now
        );
    }

    private LiquidityResourceForecastResponse
    buildProviderForecast(
            ProviderBalance providerBalance,
            List<SimulatedTransaction> transactions,
            Instant now
    ) {
        return buildForecast(
                LiquidityResourceType.PROVIDER_E_MONEY,
                providerBalance.getProvider().getProviderCode(),
                providerBalance.getProvider().getDisplayName()
                        + " E-Money",
                providerBalance.getEMoneyBalance(),
                transactions,
                now
        );
    }

    private LiquidityResourceForecastResponse buildForecast(
            LiquidityResourceType resourceType,
            String providerCode,
            String displayName,
            BigDecimal currentBalance,
            List<SimulatedTransaction> transactions,
            Instant now
    ) {
        BigDecimal rate15 =
                liquidityRateCalculator.calculateConsumptionRate(
                        transactions,
                        resourceType,
                        now.minus(15, ChronoUnit.MINUTES),
                        15
                );

        BigDecimal rate30 =
                liquidityRateCalculator.calculateConsumptionRate(
                        transactions,
                        resourceType,
                        now.minus(30, ChronoUnit.MINUTES),
                        30
                );

        BigDecimal rate60 =
                liquidityRateCalculator.calculateConsumptionRate(
                        transactions,
                        resourceType,
                        now.minus(60, ChronoUnit.MINUTES),
                        60
                );

        BigDecimal weightedRate =
                liquidityRateCalculator.calculateWeightedRate(
                        rate15,
                        rate30,
                        rate60
                );

        BigDecimal projectedRunwayMinutes = null;
        Instant estimatedShortageAt = null;

        if (weightedRate.signum() > 0) {
            projectedRunwayMinutes =
                    currentBalance.divide(
                            weightedRate,
                            RUNWAY_SCALE,
                            RoundingMode.HALF_UP
                    );

            long estimatedRunwaySeconds =
                    projectedRunwayMinutes
                            .multiply(BigDecimal.valueOf(60))
                            .setScale(
                                    0,
                                    RoundingMode.HALF_UP
                            )
                            .longValue();

            estimatedShortageAt =
                    now.plusSeconds(estimatedRunwaySeconds);
        }

        LiquidityPressureStatus status =
                determineStatus(projectedRunwayMinutes);

        int recentTransactionCount = transactions.size();

        int confidenceScore =
                calculateConfidenceScore(
                        transactions,
                        now
                );

        ForecastConfidence confidence =
                toConfidenceLevel(confidenceScore);

        List<String> explanation =
                buildExplanation(
                        displayName,
                        weightedRate,
                        projectedRunwayMinutes,
                        recentTransactionCount,
                        status
                );

        return new LiquidityResourceForecastResponse(
                resourceType,
                providerCode,
                displayName,
                currentBalance,
                rate15,
                rate30,
                rate60,
                weightedRate,
                projectedRunwayMinutes,
                estimatedShortageAt,
                status,
                confidence,
                confidenceScore,
                recentTransactionCount,
                explanation
        );
    }

    private LiquidityPressureStatus determineStatus(
            BigDecimal projectedRunwayMinutes
    ) {
        if (projectedRunwayMinutes == null) {
            return LiquidityPressureStatus.STABLE;
        }

        if (
                projectedRunwayMinutes.compareTo(
                        HIGH_PRESSURE_MINUTES
                ) <= 0
        ) {
            return LiquidityPressureStatus.HIGH_PRESSURE;
        }

        if (
                projectedRunwayMinutes.compareTo(
                        MEDIUM_PRESSURE_MINUTES
                ) <= 0
        ) {
            return LiquidityPressureStatus.MEDIUM_PRESSURE;
        }

        return LiquidityPressureStatus.STABLE;
    }

    private int calculateConfidenceScore(
            List<SimulatedTransaction> transactions,
            Instant now
    ) {
        if (transactions.isEmpty()) {
            return 25;
        }

        int score = 35;

        int transactionCount = transactions.size();

        if (transactionCount >= 20) {
            score += 35;
        } else if (transactionCount >= 8) {
            score += 25;
        } else if (transactionCount >= 3) {
            score += 15;
        } else {
            score += 5;
        }

        boolean has15MinuteData =
                hasTransactionSince(
                        transactions,
                        now.minus(
                                15,
                                ChronoUnit.MINUTES
                        )
                );

        boolean has30MinuteData =
                hasTransactionSince(
                        transactions,
                        now.minus(
                                30,
                                ChronoUnit.MINUTES
                        )
                );

        boolean hasOlderWindowData =
                transactions.stream()
                        .anyMatch(transaction ->
                                transaction.getOccurredAt()
                                        .isBefore(
                                                now.minus(
                                                        30,
                                                        ChronoUnit.MINUTES
                                                )
                                        )
                        );

        if (has15MinuteData) {
            score += 10;
        }

        if (has30MinuteData) {
            score += 10;
        }

        if (hasOlderWindowData) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    private boolean hasTransactionSince(
            List<SimulatedTransaction> transactions,
            Instant from
    ) {
        return transactions.stream()
                .anyMatch(transaction ->
                        !transaction.getOccurredAt()
                                .isBefore(from)
                );
    }

    private ForecastConfidence toConfidenceLevel(
            int confidenceScore
    ) {
        if (confidenceScore >= 80) {
            return ForecastConfidence.HIGH;
        }

        if (confidenceScore >= 55) {
            return ForecastConfidence.MEDIUM;
        }

        return ForecastConfidence.LOW;
    }

    private List<String> buildExplanation(
            String resourceName,
            BigDecimal weightedRate,
            BigDecimal runway,
            int recentTransactionCount,
            LiquidityPressureStatus status
    ) {
        List<String> explanation = new ArrayList<>();

        explanation.add(
                "Forecast uses "
                        + recentTransactionCount
                        + " transaction events from the last 60 minutes."
        );

        if (weightedRate.signum() <= 0) {
            explanation.add(
                    resourceName
                            + " does not currently show a positive net depletion trend."
            );

            explanation.add(
                    "No shortage time is projected while the current net trend continues."
            );

            return explanation;
        }

        explanation.add(
                "The weighted net consumption rate is "
                        + weightedRate
                        + " BDT per minute."
        );

        explanation.add(
                "Recent activity is weighted using 50% for 15 minutes, "
                        + "30% for 30 minutes, and 20% for 60 minutes."
        );

        if (runway != null) {
            explanation.add(
                    "At the current weighted trend, estimated runway is approximately "
                            + runway
                            + " minutes."
            );
        }

        if (
                status
                        == LiquidityPressureStatus.HIGH_PRESSURE
        ) {
            explanation.add(
                    "The estimated runway is 60 minutes or less, so the resource is classified as high pressure."
            );
        } else if (
                status
                        == LiquidityPressureStatus.MEDIUM_PRESSURE
        ) {
            explanation.add(
                    "The estimated runway is between 60 and 180 minutes, so the resource is classified as medium pressure."
            );
        }

        return explanation;
    }

    private String normalizeAgentCode(String agentCode) {
        if (agentCode == null || agentCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Agent code is required"
            );
        }

        return agentCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
