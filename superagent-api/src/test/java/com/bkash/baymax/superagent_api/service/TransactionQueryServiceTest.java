package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.TransactionPageResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private SimulatedTransactionRepository simulatedTransactionRepository;

    @Test
    void shouldReturnFilteredPaginatedTransactions() {
        Agent agent = Agent.builder()
                .id(1L)
                .agentCode("AGT-001")
                .displayName("Rahim Store")
                .area("Zindabazar")
                .district("Sylhet")
                .active(true)
                .build();

        Provider provider = Provider.builder()
                .id(1L)
                .providerCode("BKASH")
                .displayName("bKash")
                .active(true)
                .build();

        SimulatedTransaction transaction =
                SimulatedTransaction.builder()
                        .id(1L)
                        .transactionReference("SIM-TXN-001")
                        .agent(agent)
                        .provider(provider)
                        .transactionType(TransactionType.CASH_OUT)
                        .amount(new BigDecimal("2500.00"))
                        .occurredAt(
                                Instant.parse(
                                        "2026-07-11T10:00:00Z"
                                )
                        )
                        .syntheticAccountId("SIM-ACC-014")
                        .source(
                                TransactionSource.MANUAL_SIMULATION
                        )
                        .build();

        Pageable pageable = PageRequest.of(0, 20);

        when(agentRepository.existsByAgentCode("AGT-001"))
                .thenReturn(true);

        when(providerRepository.existsByProviderCode("BKASH"))
                .thenReturn(true);

        when(
                simulatedTransactionRepository.findTransactions(
                        "AGT-001",
                        "BKASH",
                        TransactionType.CASH_OUT,
                        pageable
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(transaction),
                        pageable,
                        1
                )
        );

        TransactionQueryService service =
                new TransactionQueryService(
                        agentRepository,
                        providerRepository,
                        simulatedTransactionRepository
                );

        TransactionPageResponse response =
                service.getTransactions(
                        "agt-001",
                        "bkash",
                        TransactionType.CASH_OUT,
                        pageable
                );

        assertEquals(1, response.transactions().size());

        assertEquals(
                "SIM-TXN-001",
                response.transactions()
                        .getFirst()
                        .transactionReference()
        );

        assertEquals(
                "BKASH",
                response.transactions()
                        .getFirst()
                        .providerCode()
        );

        assertEquals(1, response.totalElements());

        verify(simulatedTransactionRepository)
                .findTransactions(
                        "AGT-001",
                        "BKASH",
                        TransactionType.CASH_OUT,
                        pageable
                );
    }
}
