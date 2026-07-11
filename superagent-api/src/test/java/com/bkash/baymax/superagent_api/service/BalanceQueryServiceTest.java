package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.AgentBalanceResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceQueryServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PhysicalCashPositionRepository physicalCashPositionRepository;

    @Mock
    private ProviderBalanceRepository providerBalanceRepository;

    @Test
    void shouldReturnPhysicalCashAndSeparateProviderBalances() {
        Agent agent = Agent.builder()
                .id(1L)
                .agentCode("AGT-001")
                .displayName("Rahim Store")
                .area("Zindabazar")
                .district("Sylhet")
                .active(true)
                .build();

        Provider bkash = Provider.builder()
                .id(1L)
                .providerCode("BKASH")
                .displayName("bKash")
                .active(true)
                .build();

        Provider nagad = Provider.builder()
                .id(2L)
                .providerCode("NAGAD")
                .displayName("Nagad")
                .active(true)
                .build();

        PhysicalCashPosition cashPosition =
                PhysicalCashPosition.builder()
                        .agent(agent)
                        .cashBalance(new BigDecimal("97500.00"))
                        .build();

        ProviderBalance bkashBalance =
                ProviderBalance.builder()
                        .agent(agent)
                        .provider(bkash)
                        .eMoneyBalance(new BigDecimal("52500.00"))
                        .build();

        ProviderBalance nagadBalance =
                ProviderBalance.builder()
                        .agent(agent)
                        .provider(nagad)
                        .eMoneyBalance(new BigDecimal("40000.00"))
                        .build();

        when(agentRepository.existsByAgentCode("AGT-001"))
                .thenReturn(true);

        when(
                physicalCashPositionRepository
                        .findByAgentAgentCode("AGT-001")
        ).thenReturn(Optional.of(cashPosition));

        when(
                providerBalanceRepository
                        .findAllByAgentAgentCode("AGT-001")
        ).thenReturn(List.of(nagadBalance, bkashBalance));

        Clock clock = Clock.fixed(
                Instant.parse("2026-07-11T10:00:00Z"),
                ZoneOffset.UTC
        );

        BalanceQueryService service =
                new BalanceQueryService(
                        agentRepository,
                        physicalCashPositionRepository,
                        providerBalanceRepository,
                        clock
                );

        AgentBalanceResponse response =
                service.getAgentBalances("agt-001");

        assertEquals(
                new BigDecimal("97500.00"),
                response.physicalCashBalance()
        );

        assertEquals(
                2,
                response.providerBalances().size()
        );

        assertEquals(
                "BKASH",
                response.providerBalances()
                        .getFirst()
                        .providerCode()
        );

        assertEquals(
                new BigDecimal("52500.00"),
                response.providerBalances()
                        .getFirst()
                        .eMoneyBalance()
        );
    }
}
