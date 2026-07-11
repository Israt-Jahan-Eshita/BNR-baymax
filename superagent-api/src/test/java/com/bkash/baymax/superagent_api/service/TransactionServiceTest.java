package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.CreateSimulatedTransactionRequest;
import com.bkash.baymax.superagent_api.dto.response.SimulatedTransactionResponse;
import com.bkash.baymax.superagent_api.event.TransactionPersistedEvent;
import com.bkash.baymax.superagent_api.exception.InsufficientLiquidityException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private PhysicalCashPositionRepository
            physicalCashPositionRepository;

    @Mock
    private ProviderBalanceRepository
            providerBalanceRepository;

    @Mock
    private SimulatedTransactionRepository
            simulatedTransactionRepository;

    @Mock
    private ApplicationEventPublisher
            applicationEventPublisher;

    private Agent agent;
    private Provider provider;
    private PhysicalCashPosition cashPosition;
    private ProviderBalance providerBalance;

    @BeforeEach
    void setUp() {
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

        cashPosition =
                PhysicalCashPosition.builder()
                        .agent(agent)
                        .cashBalance(
                                new BigDecimal("100000.00")
                        )
                        .build();

        providerBalance =
                ProviderBalance.builder()
                        .agent(agent)
                        .provider(provider)
                        .eMoneyBalance(
                                new BigDecimal("50000.00")
                        )
                        .build();
    }

    @Test
    void cashOutShouldDecreaseCashAndIncreaseProviderEMoney() {
        prepareSuccessfulTransaction();

        SimulatedTransactionResponse response =
                createService().createManualTransaction(
                        request(
                                TransactionType.CASH_OUT,
                                "2500.00"
                        )
                );

        assertEquals(
                new BigDecimal("97500.00"),
                cashPosition.getCashBalance()
        );

        assertEquals(
                new BigDecimal("52500.00"),
                providerBalance.getEMoneyBalance()
        );

        assertEquals(
                TransactionType.CASH_OUT,
                response.type()
        );

        assertEquals(
                TransactionSource.MANUAL_SIMULATION,
                response.source()
        );

        assertEquals(
                NOW,
                response.occurredAt()
        );
    }

    @Test
    void cashInShouldIncreaseCashAndDecreaseProviderEMoney() {
        prepareSuccessfulTransaction();

        createService().createManualTransaction(
                request(
                        TransactionType.CASH_IN,
                        "2500.00"
                )
        );

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
    void insufficientPhysicalCashShouldRejectTransactionAndNotPublishEvent() {
        cashPosition.setCashBalance(
                new BigDecimal("1000.00")
        );

        prepareLookups();

        assertThrows(
                InsufficientLiquidityException.class,
                () ->
                        createService()
                                .createManualTransaction(
                                        request(
                                                TransactionType.CASH_OUT,
                                                "2500.00"
                                        )
                                )
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
        ).save(any(SimulatedTransaction.class));

        verify(
                applicationEventPublisher,
                never()
        ).publishEvent(any());
    }

    @Test
    void insufficientProviderEMoneyShouldRejectTransactionAndNotPublishEvent() {
        providerBalance.setEMoneyBalance(
                new BigDecimal("1000.00")
        );

        prepareLookups();

        assertThrows(
                InsufficientLiquidityException.class,
                () ->
                        createService()
                                .createManualTransaction(
                                        request(
                                                TransactionType.CASH_IN,
                                                "2500.00"
                                        )
                                )
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
        ).save(any(SimulatedTransaction.class));

        verify(
                applicationEventPublisher,
                never()
        ).publishEvent(any());
    }

    @Test
    void successfulTransactionShouldPublishAnalyticsEventAfterRepositorySave() {
        prepareSuccessfulTransaction();

        SimulatedTransactionResponse response =
                createService().createManualTransaction(
                        request(
                                TransactionType.CASH_OUT,
                                "2500.00"
                        )
                );

        ArgumentCaptor<TransactionPersistedEvent>
                eventCaptor =
                ArgumentCaptor.forClass(
                        TransactionPersistedEvent.class
                );

        InOrder inOrder =
                inOrder(
                        simulatedTransactionRepository,
                        applicationEventPublisher
                );

        inOrder.verify(
                simulatedTransactionRepository
        ).save(any(SimulatedTransaction.class));

        inOrder.verify(
                applicationEventPublisher
        ).publishEvent(
                eventCaptor.capture()
        );

        TransactionPersistedEvent event =
                eventCaptor.getValue();

        assertEquals(
                "AGT-001",
                event.agentCode()
        );

        assertEquals(
                response.transactionReference(),
                event.transactionReference()
        );

        assertEquals(
                NOW,
                event.occurredAt()
        );
    }

    private void prepareSuccessfulTransaction() {
        prepareLookups();

        when(
                simulatedTransactionRepository
                        .save(any(SimulatedTransaction.class))
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );
    }

    private void prepareLookups() {
        when(
                agentRepository
                        .findByAgentCode("AGT-001")
        ).thenReturn(
                Optional.of(agent)
        );

        when(
                providerRepository
                        .findByProviderCode("BKASH")
        ).thenReturn(
                Optional.of(provider)
        );

        when(
                physicalCashPositionRepository
                        .findByAgentAgentCode("AGT-001")
        ).thenReturn(
                Optional.of(cashPosition)
        );

        when(
                providerBalanceRepository
                        .findByAgentAgentCodeAndProviderProviderCode(
                                "AGT-001",
                                "BKASH"
                        )
        ).thenReturn(
                Optional.of(providerBalance)
        );
    }

    private TransactionService createService() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        return new TransactionService(
                agentRepository,
                providerRepository,
                physicalCashPositionRepository,
                providerBalanceRepository,
                simulatedTransactionRepository,
                clock,
                applicationEventPublisher
        );
    }

    private CreateSimulatedTransactionRequest request(
            TransactionType type,
            String amount
    ) {
        return new CreateSimulatedTransactionRequest(
                "agt-001",
                "bkash",
                type,
                new BigDecimal(amount),
                "SIM-ACC-014"
        );
    }
}
