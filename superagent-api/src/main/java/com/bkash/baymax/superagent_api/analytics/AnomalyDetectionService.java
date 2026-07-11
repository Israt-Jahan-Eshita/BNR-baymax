package com.bkash.baymax.superagent_api.analytics;

import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.ProviderDataHealth;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.model.enums.SignalConfidence;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ProviderDataHealthRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private static final BigDecimal VELOCITY_MULTIPLIER_THRESHOLD =
            new BigDecimal("3.00");

    private static final BigDecimal AMOUNT_MULTIPLIER_THRESHOLD =
            new BigDecimal("2.50");

    private static final int MINIMUM_VELOCITY_TRANSACTION_COUNT = 8;

    private static final BigDecimal REPEATED_AMOUNT_TOLERANCE =
            new BigDecimal("0.02");

    private static final int MINIMUM_REPEATED_CLUSTER_COUNT = 6;

    private static final BigDecimal MINIMUM_REPEATED_RATIO =
            new BigDecimal("0.50");

    private final AgentRepository agentRepository;
    private final SimulatedTransactionRepository
            simulatedTransactionRepository;
    private final ProviderDataHealthRepository
            providerDataHealthRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<DetectedSignal> detect(
            String requestedAgentCode
    ) {
        String agentCode = normalizeAgentCode(
                requestedAgentCode
        );

        Agent agent = agentRepository
                .findByAgentCode(agentCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agent not found: " + agentCode
                        )
                );

        Instant now = Instant.now(clock);
        Instant sixtyMinutesAgo =
                now.minus(60, ChronoUnit.MINUTES);
        Instant fifteenMinutesAgo =
                now.minus(15, ChronoUnit.MINUTES);

        List<SimulatedTransaction> transactions =
                simulatedTransactionRepository
                        .findAllByAgentAgentCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
                                agentCode,
                                sixtyMinutesAgo,
                                now
                        );

        Map<String, ProviderDataHealthStatus> healthByProvider =
                loadProviderHealth(agentCode);

        Map<String, List<SimulatedTransaction>>
                transactionsByProvider =
                transactions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        transaction ->
                                                transaction
                                                        .getProvider()
                                                        .getProviderCode()
                                )
                        );

        List<DetectedSignal> signals =
                new ArrayList<>();

        for (
                List<SimulatedTransaction> providerTransactions
                : transactionsByProvider.values()
        ) {
            if (providerTransactions.isEmpty()) {
                continue;
            }

            SimulatedTransaction firstTransaction =
                    providerTransactions.getFirst();

            String providerCode =
                    firstTransaction
                            .getProvider()
                            .getProviderCode();

            ProviderDataHealthStatus healthStatus =
                    healthByProvider.getOrDefault(
                            providerCode,
                            ProviderDataHealthStatus.MISSING
                    );

            detectCashOutVelocitySpike(
                    agent,
                    providerTransactions,
                    fifteenMinutesAgo,
                    now,
                    healthStatus
            ).ifPresent(signals::add);

            detectRepeatedAmountCluster(
                    agent,
                    providerTransactions,
                    fifteenMinutesAgo,
                    now,
                    healthStatus
            ).ifPresent(signals::add);
        }

        return signals;
    }

    private java.util.Optional<DetectedSignal>
    detectCashOutVelocitySpike(
            Agent agent,
            List<SimulatedTransaction> providerTransactions,
            Instant fifteenMinutesAgo,
            Instant now,
            ProviderDataHealthStatus healthStatus
    ) {
        List<SimulatedTransaction> currentCashOuts =
                providerTransactions.stream()
                        .filter(transaction ->
                                transaction.getTransactionType()
                                        == TransactionType.CASH_OUT
                        )
                        .filter(transaction ->
                                !transaction.getOccurredAt()
                                        .isBefore(fifteenMinutesAgo)
                        )
                        .toList();

        List<SimulatedTransaction> baselineCashOuts =
                providerTransactions.stream()
                        .filter(transaction ->
                                transaction.getTransactionType()
                                        == TransactionType.CASH_OUT
                        )
                        .filter(transaction ->
                                transaction.getOccurredAt()
                                        .isBefore(fifteenMinutesAgo)
                        )
                        .toList();

        int currentCount = currentCashOuts.size();

        BigDecimal baselineCountPer15 =
                BigDecimal.valueOf(
                                baselineCashOuts.size()
                        )
                        .divide(
                                new BigDecimal("3"),
                                4,
                                RoundingMode.HALF_UP
                        );

        BigDecimal countMultiplier =
                BigDecimal.valueOf(currentCount)
                        .divide(
                                baselineCountPer15.max(
                                        BigDecimal.ONE
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal currentAmount =
                sumAmounts(currentCashOuts);

        BigDecimal baselineAmountPer15 =
                sumAmounts(baselineCashOuts)
                        .divide(
                                new BigDecimal("3"),
                                4,
                                RoundingMode.HALF_UP
                        );

        BigDecimal amountMultiplier =
                currentAmount.divide(
                        baselineAmountPer15.max(
                                new BigDecimal("1000.00")
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        boolean detected =
                currentCount
                        >= MINIMUM_VELOCITY_TRANSACTION_COUNT
                && countMultiplier.compareTo(
                        VELOCITY_MULTIPLIER_THRESHOLD
                ) >= 0
                && amountMultiplier.compareTo(
                        AMOUNT_MULTIPLIER_THRESHOLD
                ) >= 0;

        if (!detected) {
            return java.util.Optional.empty();
        }

        AlertSeverity severity =
                determineVelocitySeverity(
                        currentCount,
                        countMultiplier,
                        amountMultiplier
                );

        int confidenceScore =
                calculateConfidenceScore(
                        currentCashOuts.size(),
                        baselineCashOuts.size(),
                        healthStatus
                );

        String providerName =
                currentCashOuts
                        .getFirst()
                        .getProvider()
                        .getDisplayName();

        List<String> evidence = List.of(
                currentCount
                        + " CASH_OUT transactions occurred in the last 15 minutes.",

                "The previous 45 minutes imply an average of "
                        + baselineCountPer15.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        + " CASH_OUT transactions per 15 minutes.",

                "Current cash-out velocity is "
                        + countMultiplier
                        + "x the recent baseline.",

                "Current 15-minute cash-out amount is BDT "
                        + currentAmount.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        + ", which is "
                        + amountMultiplier
                        + "x the recent baseline amount."
        );

        return java.util.Optional.of(
                new DetectedSignal(
                        agent,
                        currentCashOuts
                                .getFirst()
                                .getProvider(),
                        AlertType.CASH_OUT_VELOCITY_SPIKE,
                        severity,
                        toConfidence(confidenceScore),
                        confidenceScore,
                        providerName
                                + " cash-out velocity spike",
                        "Recent cash-out activity is materially higher than the provider's recent transaction baseline.",
                        evidence,
                        "A festival, salary period, local event, or temporary customer rush may legitimately increase cash-out demand.",
                        buildUncertainty(healthStatus),
                        "Review the recent provider cash-out transactions and verify whether a known operational demand event explains the spike. Do not treat this signal as confirmed fraud.",
                        fifteenMinutesAgo,
                        now
                )
        );
    }

    private java.util.Optional<DetectedSignal>
    detectRepeatedAmountCluster(
            Agent agent,
            List<SimulatedTransaction> providerTransactions,
            Instant fifteenMinutesAgo,
            Instant now,
            ProviderDataHealthStatus healthStatus
    ) {
        List<SimulatedTransaction> recentTransactions =
                providerTransactions.stream()
                        .filter(transaction ->
                                !transaction.getOccurredAt()
                                        .isBefore(fifteenMinutesAgo)
                        )
                        .sorted(
                                Comparator.comparing(
                                        SimulatedTransaction::getAmount
                                )
                        )
                        .toList();

        if (
                recentTransactions.size()
                        < MINIMUM_REPEATED_CLUSTER_COUNT
        ) {
            return java.util.Optional.empty();
        }

        List<SimulatedTransaction> cluster =
                findLargestNearIdenticalCluster(
                        recentTransactions
                );

        int clusterCount = cluster.size();

        Set<String> uniqueAccounts =
                cluster.stream()
                        .map(
                                SimulatedTransaction::
                                        getSyntheticAccountId
                        )
                        .collect(Collectors.toSet());

        BigDecimal repeatedRatio =
                BigDecimal.valueOf(clusterCount)
                        .divide(
                                BigDecimal.valueOf(
                                        recentTransactions.size()
                                ),
                                4,
                                RoundingMode.HALF_UP
                        );

        boolean detected =
                clusterCount
                        >= MINIMUM_REPEATED_CLUSTER_COUNT
                && uniqueAccounts.size() <= 3
                && repeatedRatio.compareTo(
                        MINIMUM_REPEATED_RATIO
                ) >= 0;

        if (!detected) {
            return java.util.Optional.empty();
        }

        AlertSeverity severity =
                determineRepeatedClusterSeverity(
                        clusterCount,
                        uniqueAccounts.size(),
                        repeatedRatio
                );

        int confidenceScore =
                calculateConfidenceScore(
                        recentTransactions.size(),
                        clusterCount,
                        healthStatus
                );

        BigDecimal minimumAmount =
                cluster.getFirst().getAmount();

        BigDecimal maximumAmount =
                cluster.getLast().getAmount();

        String providerName =
                cluster.getFirst()
                        .getProvider()
                        .getDisplayName();

        List<String> evidence = List.of(
                clusterCount
                        + " near-identical transactions were identified within the last 15 minutes.",

                "Cluster amounts range from BDT "
                        + minimumAmount.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        + " to BDT "
                        + maximumAmount.setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        + ".",

                "The repeated-amount cluster represents "
                        + repeatedRatio
                                .multiply(
                                        new BigDecimal("100")
                                )
                                .setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        + "% of recent provider transactions.",

                "The cluster is concentrated across "
                        + uniqueAccounts.size()
                        + " synthetic account identifiers."
        );

        return java.util.Optional.of(
                new DetectedSignal(
                        agent,
                        cluster.getFirst().getProvider(),
                        AlertType.REPEATED_AMOUNT_CLUSTER,
                        severity,
                        toConfidence(confidenceScore),
                        confidenceScore,
                        providerName
                                + " repeated amount cluster",
                        "A concentrated cluster of near-identical transaction amounts requires human review.",
                        evidence,
                        "A fixed fee, batch collection, admission payment, salary-related activity, or event-linked service can legitimately create repeated amounts.",
                        buildUncertainty(healthStatus),
                        "Review the clustered transactions and synthetic account concentration, and verify whether a known fixed-price or event-related payment pattern explains the repetition.",
                        fifteenMinutesAgo,
                        now
                )
        );
    }

    private List<SimulatedTransaction>
    findLargestNearIdenticalCluster(
            List<SimulatedTransaction> sortedTransactions
    ) {
        List<SimulatedTransaction> largestCluster =
                List.of();

        for (
                int startIndex = 0;
                startIndex < sortedTransactions.size();
                startIndex++
        ) {
            BigDecimal baseAmount =
                    sortedTransactions
                            .get(startIndex)
                            .getAmount();

            BigDecimal maximumAllowedAmount =
                    baseAmount.multiply(
                            BigDecimal.ONE.add(
                                    REPEATED_AMOUNT_TOLERANCE
                            )
                    );

            List<SimulatedTransaction> candidate =
                    new ArrayList<>();

            for (
                    int index = startIndex;
                    index < sortedTransactions.size();
                    index++
            ) {
                SimulatedTransaction transaction =
                        sortedTransactions.get(index);

                if (
                        transaction.getAmount()
                                .compareTo(
                                        maximumAllowedAmount
                                ) > 0
                ) {
                    break;
                }

                candidate.add(transaction);
            }

            if (
                    candidate.size()
                            > largestCluster.size()
            ) {
                largestCluster =
                        List.copyOf(candidate);
            }
        }

        return largestCluster;
    }

    private AlertSeverity determineVelocitySeverity(
            int currentCount,
            BigDecimal countMultiplier,
            BigDecimal amountMultiplier
    ) {
        if (
                currentCount >= 20
                || countMultiplier.compareTo(
                        new BigDecimal("5.00")
                ) >= 0
                || amountMultiplier.compareTo(
                        new BigDecimal("5.00")
                ) >= 0
        ) {
            return AlertSeverity.HIGH;
        }

        return AlertSeverity.MEDIUM;
    }

    private AlertSeverity determineRepeatedClusterSeverity(
            int clusterCount,
            int uniqueAccountCount,
            BigDecimal repeatedRatio
    ) {
        if (
                (
                        clusterCount >= 9
                        && uniqueAccountCount <= 2
                )
                || repeatedRatio.compareTo(
                        new BigDecimal("0.75")
                ) >= 0
        ) {
            return AlertSeverity.HIGH;
        }

        return AlertSeverity.MEDIUM;
    }

    private int calculateConfidenceScore(
            int currentSampleSize,
            int comparisonSampleSize,
            ProviderDataHealthStatus healthStatus
    ) {
        int score = 45;

        score += Math.min(
                currentSampleSize,
                25
        );

        score += Math.min(
                comparisonSampleSize,
                20
        );

        return switch (healthStatus) {
            case LIVE ->
                    Math.min(score, 95);

            case DELAYED ->
                    Math.max(
                            Math.min(score, 95) - 20,
                            0
                    );

            case MISSING ->
                    Math.min(score, 35);

            case CONFLICTING ->
                    Math.min(score, 30);
        };
    }

    private SignalConfidence toConfidence(
            int confidenceScore
    ) {
        if (confidenceScore >= 80) {
            return SignalConfidence.HIGH;
        }

        if (confidenceScore >= 55) {
            return SignalConfidence.MEDIUM;
        }

        return SignalConfidence.LOW;
    }

    private String buildUncertainty(
            ProviderDataHealthStatus healthStatus
    ) {
        return switch (healthStatus) {
            case LIVE ->
                    "The signal is based on a short recent synthetic baseline and may change as more transactions arrive.";

            case DELAYED ->
                    "Provider data is delayed, so recent activity may be incomplete and signal confidence is reduced.";

            case MISSING ->
                    "Provider data health is missing. The observed pattern may be incomplete and only a low-confidence review signal is produced.";

            case CONFLICTING ->
                    "Provider data contains conflicting information. High-confidence interpretation is withheld pending data review.";
        };
    }

    private Map<String, ProviderDataHealthStatus>
    loadProviderHealth(
            String agentCode
    ) {
        Map<String, ProviderDataHealthStatus> result =
                new HashMap<>();

        for (
                ProviderDataHealth health
                : providerDataHealthRepository
                        .findAllByAgentAgentCode(agentCode)
        ) {
            result.put(
                    health.getProvider().getProviderCode(),
                    health.getStatus()
            );
        }

        return result;
    }

    private BigDecimal sumAmounts(
            List<SimulatedTransaction> transactions
    ) {
        return transactions.stream()
                .map(SimulatedTransaction::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private String normalizeAgentCode(
            String agentCode
    ) {
        if (
                agentCode == null
                || agentCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Agent code is required"
            );
        }

        return agentCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
