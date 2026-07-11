package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import com.bkash.baymax.superagent_api.model.ProviderDataHealth;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.repository.ProviderDataHealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoDataSeedService implements CommandLineRunner {

    private static final String DEMO_AGENT_CODE = "AGT-001";

    @Override
    public void run(String... args) throws Exception {
        seedBaselineIfMissing();
    }

    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final ProviderBalanceRepository providerBalanceRepository;
    private final PhysicalCashPositionRepository physicalCashPositionRepository;
    private final SimulatedTransactionRepository simulatedTransactionRepository;
    private final ProviderDataHealthRepository providerDataHealthRepository;

    @Transactional
    public void seedBaselineIfMissing() {
        Agent agent = getOrCreateAgent();

        Provider bkash = getOrCreateProvider(
                "BKASH",
                "bKash"
        );

        Provider nagad = getOrCreateProvider(
                "NAGAD",
                "Nagad"
        );

        Provider rocket = getOrCreateProvider(
                "ROCKET",
                "Rocket"
        );

        createProviderBalanceIfMissing(
                agent,
                bkash,
                new BigDecimal("50000.00")
        );

        createProviderBalanceIfMissing(
                agent,
                nagad,
                new BigDecimal("40000.00")
        );

        createProviderBalanceIfMissing(
                agent,
                rocket,
                new BigDecimal("30000.00")
        );

        createProviderDataHealthIfMissing(
                agent,
                bkash
        );

        createProviderDataHealthIfMissing(
                agent,
                nagad
        );

        createProviderDataHealthIfMissing(
                agent,
                rocket
        );

        createPhysicalCashIfMissing(
                agent,
                new BigDecimal("100000.00")
        );

        seedBaselineTransactions(
                agent,
                bkash,
                nagad,
                rocket
        );
    }

    private Agent getOrCreateAgent() {
        return agentRepository.findByAgentCode(DEMO_AGENT_CODE)
                .orElseGet(() -> agentRepository.save(
                        Agent.builder()
                                .agentCode(DEMO_AGENT_CODE)
                                .displayName("Rahim Store")
                                .area("Zindabazar")
                                .district("Sylhet")
                                .active(true)
                                .build()
                ));
    }

    private Provider getOrCreateProvider(
            String providerCode,
            String displayName
    ) {
        return providerRepository.findByProviderCode(providerCode)
                .orElseGet(() -> providerRepository.save(
                        Provider.builder()
                                .providerCode(providerCode)
                                .displayName(displayName)
                                .active(true)
                                .build()
                ));
    }

    private void createProviderBalanceIfMissing(
            Agent agent,
            Provider provider,
            BigDecimal balance
    ) {
        boolean exists = providerBalanceRepository
                .existsByAgentIdAndProviderId(
                        agent.getId(),
                        provider.getId()
                );

        if (exists) {
            return;
        }

        ProviderBalance providerBalance =
                ProviderBalance.builder()
                        .agent(agent)
                        .provider(provider)
                        .eMoneyBalance(balance)
                        .build();

        providerBalanceRepository.save(providerBalance);
    }

    private void createProviderDataHealthIfMissing(
            Agent agent,
            Provider provider
    ) {
        boolean exists =
                providerDataHealthRepository
                        .existsByAgentIdAndProviderId(
                                agent.getId(),
                                provider.getId()
                        );

        if (exists) {
            return;
        }

        ProviderDataHealth dataHealth =
                ProviderDataHealth.builder()
                        .agent(agent)
                        .provider(provider)
                        .status(
                                ProviderDataHealthStatus.LIVE
                        )
                        .lastSuccessfulUpdateAt(
                                Instant.now()
                        )
                        .delayMinutes(0)
                        .build();

        providerDataHealthRepository.save(dataHealth);
    }

    private void createPhysicalCashIfMissing(
            Agent agent,
            BigDecimal cashBalance
    ) {
        if (physicalCashPositionRepository.existsByAgentId(
                agent.getId()
        )) {
            return;
        }

        PhysicalCashPosition cashPosition =
                PhysicalCashPosition.builder()
                        .agent(agent)
                        .cashBalance(cashBalance)
                        .build();

        physicalCashPositionRepository.save(cashPosition);
    }

    private void seedBaselineTransactions(
            Agent agent,
            Provider bkash,
            Provider nagad,
            Provider rocket
    ) {
        Instant anchor = Instant.now()
                .truncatedTo(ChronoUnit.MINUTES);

        List<BaselineTransactionSeed> seeds = List.of(
                new BaselineTransactionSeed(
                        "BASE-TXN-001",
                        bkash,
                        TransactionType.CASH_OUT,
                        "2500.00",
                        115,
                        "SIM-ACC-001"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-002",
                        nagad,
                        TransactionType.CASH_IN,
                        "1800.00",
                        108,
                        "SIM-ACC-002"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-003",
                        rocket,
                        TransactionType.CASH_OUT,
                        "1200.00",
                        99,
                        "SIM-ACC-003"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-004",
                        bkash,
                        TransactionType.CASH_IN,
                        "3000.00",
                        91,
                        "SIM-ACC-004"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-005",
                        nagad,
                        TransactionType.CASH_OUT,
                        "2200.00",
                        83,
                        "SIM-ACC-005"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-006",
                        bkash,
                        TransactionType.CASH_OUT,
                        "1500.00",
                        75,
                        "SIM-ACC-006"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-007",
                        rocket,
                        TransactionType.CASH_IN,
                        "1750.00",
                        67,
                        "SIM-ACC-007"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-008",
                        nagad,
                        TransactionType.CASH_IN,
                        "2100.00",
                        59,
                        "SIM-ACC-008"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-009",
                        bkash,
                        TransactionType.CASH_OUT,
                        "2800.00",
                        51,
                        "SIM-ACC-009"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-010",
                        rocket,
                        TransactionType.CASH_OUT,
                        "1300.00",
                        44,
                        "SIM-ACC-010"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-011",
                        nagad,
                        TransactionType.CASH_OUT,
                        "1900.00",
                        36,
                        "SIM-ACC-011"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-012",
                        bkash,
                        TransactionType.CASH_IN,
                        "2400.00",
                        29,
                        "SIM-ACC-012"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-013",
                        rocket,
                        TransactionType.CASH_IN,
                        "1600.00",
                        23,
                        "SIM-ACC-013"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-014",
                        bkash,
                        TransactionType.CASH_OUT,
                        "2000.00",
                        17,
                        "SIM-ACC-014"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-015",
                        nagad,
                        TransactionType.CASH_OUT,
                        "2300.00",
                        11,
                        "SIM-ACC-015"
                ),
                new BaselineTransactionSeed(
                        "BASE-TXN-016",
                        rocket,
                        TransactionType.CASH_OUT,
                        "1400.00",
                        6,
                        "SIM-ACC-016"
                )
        );

        for (BaselineTransactionSeed seed : seeds) {
            if (simulatedTransactionRepository
                    .existsByTransactionReference(seed.reference())) {
                continue;
            }

            SimulatedTransaction transaction =
                    SimulatedTransaction.builder()
                            .transactionReference(seed.reference())
                            .agent(agent)
                            .provider(seed.provider())
                            .transactionType(seed.type())
                            .amount(new BigDecimal(seed.amount()))
                            .occurredAt(
                                    anchor.minus(
                                            seed.minutesAgo(),
                                            ChronoUnit.MINUTES
                                    )
                            )
                            .syntheticAccountId(
                                    seed.syntheticAccountId()
                            )
                            .source(TransactionSource.BASELINE)
                            .build();

            simulatedTransactionRepository.save(transaction);
        }
    }

    private record BaselineTransactionSeed(
            String reference,
            Provider provider,
            TransactionType type,
            String amount,
            long minutesAgo,
            String syntheticAccountId
    ) {
    }
}
