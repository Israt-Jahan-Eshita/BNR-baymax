package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.analytics.LiquidityRateCalculator;
import com.bkash.baymax.superagent_api.dto.response.LiquidityForecastResponse;
import com.bkash.baymax.superagent_api.dto.response.LiquidityResourceForecastResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.LiquidityPressureStatus;
import com.bkash.baymax.superagent_api.model.enums.LiquidityResourceType;
import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidityForecastServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PhysicalCashPositionRepository
            physicalCashPositionRepository;

    @Mock
    private ProviderBalanceRepository
            providerBalanceRepository;

    @Mock
    private SimulatedTransactionRepository
            simulatedTransactionRepository;

    @Test
    void shouldForecastHighPhysicalCashPressure() {
        Agent agent = createAgent();

        PhysicalCashPosition cashPosition =
                PhysicalCashPosition.builder()
                        .agent(agent)
                        .cashBalance(
                                new BigDecimal("30000.00")
                        )
                        .build();

        List<SimulatedTransaction> transactions =
                new ArrayList<>();

        for (int index = 0; index < 20; index++) {
            transactions.add(
                    createTransaction(
                            agent,
                            TransactionType.CASH_OUT,
                            "3000.00",
                            NOW.minusSeconds(
                                    60L * (index + 1)
                            )
                    )
            );
        }

        when(agentRepository.existsByAgentCode("AGT-001"))
                .thenReturn(true);

        when(
                physicalCashPositionRepository
                        .findByAgentAgentCode("AGT-001")
        ).thenReturn(Optional.of(cashPosition));

        when(
                providerBalanceRepository
                        .findAllByAgentAgentCode("AGT-001")
        ).thenReturn(List.of());

        when(
                simulatedTransactionRepository
                        .findAllByAgentAgentCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
                                "AGT-001",
                                NOW.minusSeconds(3600),
                                NOW
                        )
        ).thenReturn(transactions);

        LiquidityForecastService service =
                createService();

        LiquidityForecastResponse response =
                service.getForecast("agt-001");

        LiquidityResourceForecastResponse forecast =
                response.resources().getFirst();

        assertEquals(
                LiquidityResourceType.PHYSICAL_CASH,
                forecast.resourceType()
        );

        assertEquals(
                LiquidityPressureStatus.HIGH_PRESSURE,
                forecast.status()
        );
    }

    @Test
    void shouldReturnStableWhenNetConsumptionIsNotPositive() {
        Agent agent = createAgent();

        PhysicalCashPosition cashPosition =
                PhysicalCashPosition.builder()
                        .agent(agent)
                        .cashBalance(
                                new BigDecimal("100000.00")
                        )
                        .build();

        List<SimulatedTransaction> transactions =
                List.of(
                        createTransaction(
                                agent,
                                TransactionType.CASH_IN,
                                "5000.00",
                                NOW.minusSeconds(300)
                        )
                );

        when(agentRepository.existsByAgentCode("AGT-001"))
                .thenReturn(true);

        when(
                physicalCashPositionRepository
                        .findByAgentAgentCode("AGT-001")
        ).thenReturn(Optional.of(cashPosition));

        when(
                providerBalanceRepository
                        .findAllByAgentAgentCode("AGT-001")
        ).thenReturn(List.of());

        when(
                simulatedTransactionRepository
                        .findAllByAgentAgentCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
                                "AGT-001",
                                NOW.minusSeconds(3600),
                                NOW
                        )
        ).thenReturn(transactions);

        LiquidityForecastService service =
                createService();

        LiquidityResourceForecastResponse forecast =
                service.getForecast("AGT-001")
                        .resources()
                        .getFirst();

        assertEquals(
                LiquidityPressureStatus.STABLE,
                forecast.status()
        );

        assertNull(
                forecast.projectedRunwayMinutes()
        );

        assertNull(
                forecast.estimatedShortageAt()
        );
    }

    private LiquidityForecastService createService() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        return new LiquidityForecastService(
                agentRepository,
                physicalCashPositionRepository,
                providerBalanceRepository,
                simulatedTransactionRepository,
                new LiquidityRateCalculator(),
                clock
        );
    }

    private Agent createAgent() {
        return Agent.builder()
                .id(1L)
                .agentCode("AGT-001")
                .displayName("Rahim Store")
                .area("Zindabazar")
                .district("Sylhet")
                .active(true)
                .build();
    }

    private SimulatedTransaction createTransaction(
            Agent agent,
            TransactionType transactionType,
            String amount,
            Instant occurredAt
    ) {
        Provider provider = Provider.builder()
                .id(1L)
                .providerCode("BKASH")
                .displayName("bKash")
                .active(true)
                .build();

        return SimulatedTransaction.builder()
                .agent(agent)
                .provider(provider)
                .transactionReference(
                        "TX-" + occurredAt.toEpochMilli()
                )
                .transactionType(transactionType)
                .amount(new BigDecimal(amount))
                .occurredAt(occurredAt)
                .syntheticAccountId("SIM-ACC-001")
                .source(TransactionSource.BASELINE)
                .build();
    }
}
