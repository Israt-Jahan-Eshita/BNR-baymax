package com.bkash.baymax.superagent_api.analytics;

import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderDataHealth;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ProviderDataHealthRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private SimulatedTransactionRepository simulatedTransactionRepository;

    @Mock
    private ProviderDataHealthRepository providerDataHealthRepository;

    @Test
    void shouldDetectCashOutVelocitySpike() {
        Agent agent = Agent.builder().agentCode("AGT-001").build();
        Provider provider = Provider.builder().providerCode("BKASH").build();

        when(agentRepository.findByAgentCode("AGT-001")).thenReturn(Optional.of(agent));

        List<SimulatedTransaction> transactions = new ArrayList<>();
        // Baseline: 4 transactions in 45 min = avg ~1.3 per 15 min
        for (int i = 0; i < 4; i++) {
            transactions.add(createTx(provider, TransactionType.CASH_OUT, "5000", NOW.minusSeconds(1800 + i * 60)));
        }
        // Spike: 10 transactions in 15 min
        for (int i = 0; i < 10; i++) {
            transactions.add(createTx(provider, TransactionType.CASH_OUT, "10000", NOW.minusSeconds(100 + i * 10)));
        }

        when(simulatedTransactionRepository.findAllByAgentAgentCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
                anyString(), any(Instant.class), any(Instant.class)
        )).thenReturn(transactions);

        when(providerDataHealthRepository.findAllByAgentAgentCode("AGT-001"))
                .thenReturn(List.of(
                        ProviderDataHealth.builder().provider(provider).status(ProviderDataHealthStatus.LIVE).build()
                ));

        AnomalyDetectionService service = new AnomalyDetectionService(
                agentRepository, simulatedTransactionRepository, providerDataHealthRepository, Clock.fixed(NOW, ZoneOffset.UTC)
        );

        List<DetectedSignal> signals = service.detect("AGT-001");

        assertFalse(signals.isEmpty());
        DetectedSignal signal = signals.stream().filter(s -> s.alertType() == AlertType.CASH_OUT_VELOCITY_SPIKE).findFirst().orElseThrow();
        assertEquals(AlertSeverity.HIGH, signal.severity());
    }

    private SimulatedTransaction createTx(Provider provider, TransactionType type, String amount, Instant time) {
        return SimulatedTransaction.builder()
                .provider(provider)
                .transactionType(type)
                .amount(new BigDecimal(amount))
                .occurredAt(time)
                .syntheticAccountId("ACC-01")
                .build();
    }
}
