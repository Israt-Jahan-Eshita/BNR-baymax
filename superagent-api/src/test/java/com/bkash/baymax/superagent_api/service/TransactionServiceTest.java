package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.CreateSimulatedTransactionRequest;
import com.bkash.baymax.superagent_api.dto.response.SimulatedTransactionResponse;
import com.bkash.baymax.superagent_api.exception.InsufficientLiquidityException;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderBalanceRepository providerBalanceRepository;

    @Mock
    private PhysicalCashPositionRepository physicalCashPositionRepository;

    @Mock
    private SimulatedTransactionRepository simulatedTransactionRepository;

    private TransactionService transactionService;

    private Agent agent;
    private Provider provider;
    private PhysicalCashPosition cashPosition;
    private ProviderBalance providerBalance;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                FIXED_TIME,
                ZoneOffset.UTC
        );

        transactionService = new TransactionService(
                agentRepository,
                providerRepository,
                providerBalanceRepository,
                physicalCashPositionRepository,
                simulatedTransactionRepository,
                clock
        );

        agent = Agent.builder()
                .id(1L)
                .agentCode("AGT-001")
                .displayName("Rahim Store")
                .area("Zindabazar")
                .district("Sylhet")
                .active(true)
                .build();

        provider = Provider.builder()
                .id(1L)
                .providerCode("BKASH")
                .displayName("bKash")
                .active(true)
                .build();

        cashPosition = PhysicalCashPosition.builder()
                .id(1L)
                .agent(agent)
                .cashBalance(new BigDecimal("100000.00"))
                .build();

        providerBalance = ProviderBalance.builder()
                .id(1L)
                .agent(agent)
                .provider(provider)
                .eMoneyBalance(new BigDecimal("50000.00"))
                .build();

        when(agentRepository.findByAgentCode("AGT-001"))
                .thenReturn(Optional.of(agent));

        when(providerRepository.findByProviderCode("BKASH"))
                .thenReturn(Optional.of(provider));

        when(
                physicalCashPositionRepository
                        .findByAgentAgentCode("AGT-001")
        ).thenReturn(Optional.of(cashPosition));

        when(
                providerBalanceRepository
                        .findByAgentAgentCodeAndProviderProviderCode(
                                "AGT-001",
                                "BKASH"
                        )
        ).thenReturn(Optional.of(providerBalance));

        org.mockito.Mockito.lenient().when(simulatedTransactionRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void cashOutShouldDecreaseCashAndIncreaseProviderEMoney() {
        CreateSimulatedTransactionRequest request =
                new CreateSimulatedTransactionRequest(
                        "AGT-001",
                        "BKASH",
                        TransactionType.CASH_OUT,
                        new BigDecimal("2500.00"),
                        "SIM-ACC-014"
                );

        SimulatedTransactionResponse response =
                transactionService.createManualTransaction(request);

        assertEquals(
                new BigDecimal("97500.00"),
                cashPosition.getCashBalance()
        );

        assertEquals(
                new BigDecimal("52500.00"),
                providerBalance.getEMoneyBalance()
        );

        assertEquals(
                FIXED_TIME,
                response.occurredAt()
        );

        verify(simulatedTransactionRepository)
                .save(any(SimulatedTransaction.class));
    }

    @Test
    void cashInShouldIncreaseCashAndDecreaseProviderEMoney() {
        CreateSimulatedTransactionRequest request =
                new CreateSimulatedTransactionRequest(
                        "AGT-001",
                        "BKASH",
                        TransactionType.CASH_IN,
                        new BigDecimal("2500.00"),
                        "SIM-ACC-014"
                );

        transactionService.createManualTransaction(request);

        assertEquals(
                new BigDecimal("102500.00"),
                cashPosition.getCashBalance()
        );

        assertEquals(
                new BigDecimal("47500.00"),
                providerBalance.getEMoneyBalance()
        );
    }

    @Test
    void cashOutShouldFailWhenPhysicalCashIsInsufficient() {
        cashPosition.setCashBalance(
                new BigDecimal("1000.00")
        );

        CreateSimulatedTransactionRequest request =
                new CreateSimulatedTransactionRequest(
                        "AGT-001",
                        "BKASH",
                        TransactionType.CASH_OUT,
                        new BigDecimal("2500.00"),
                        "SIM-ACC-014"
                );

        assertThrows(
                InsufficientLiquidityException.class,
                () -> transactionService
                        .createManualTransaction(request)
        );

        assertEquals(
                new BigDecimal("1000.00"),
                cashPosition.getCashBalance()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                providerBalance.getEMoneyBalance()
        );

        verify(
                simulatedTransactionRepository,
                never()
        ).save(any());
    }

    @Test
    void cashInShouldFailWhenProviderEMoneyIsInsufficient() {
        providerBalance.setEMoneyBalance(
                new BigDecimal("1000.00")
        );

        CreateSimulatedTransactionRequest request =
                new CreateSimulatedTransactionRequest(
                        "AGT-001",
                        "BKASH",
                        TransactionType.CASH_IN,
                        new BigDecimal("2500.00"),
                        "SIM-ACC-014"
                );

        assertThrows(
                InsufficientLiquidityException.class,
                () -> transactionService
                        .createManualTransaction(request)
        );

        assertEquals(
                new BigDecimal("100000.00"),
                cashPosition.getCashBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                providerBalance.getEMoneyBalance()
        );

        verify(
                simulatedTransactionRepository,
                never()
        ).save(any());
    }
}
