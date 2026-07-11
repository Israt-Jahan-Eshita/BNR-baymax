package com.bkash.baymax.superagent_api.listener;

import com.bkash.baymax.superagent_api.event.TransactionPersistedEvent;
import com.bkash.baymax.superagent_api.service.AnalyticsCoordinatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionAnalyticsListenerTest {

    private static final Instant OCCURRED_AT =
            Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AnalyticsCoordinatorService
            analyticsCoordinatorService;

    @Test
    void shouldEvaluateAnomaliesForCommittedTransactionAgent() {
        when(
                analyticsCoordinatorService
                        .evaluateAnomalies("AGT-001")
        ).thenReturn(List.of());

        TransactionAnalyticsListener listener =
                new TransactionAnalyticsListener(
                        analyticsCoordinatorService
                );

        listener.onTransactionPersisted(
                createEvent()
        );

        verify(
                analyticsCoordinatorService
        ).evaluateAnomalies("AGT-001");
    }

    @Test
    void analyticsFailureShouldNotEscapeCommittedTransactionListener() {
        when(
                analyticsCoordinatorService
                        .evaluateAnomalies("AGT-001")
        ).thenThrow(
                new IllegalStateException(
                        "Synthetic analytics failure"
                )
        );

        TransactionAnalyticsListener listener =
                new TransactionAnalyticsListener(
                        analyticsCoordinatorService
                );

        assertDoesNotThrow(
                () ->
                        listener.onTransactionPersisted(
                                createEvent()
                        )
        );
    }

    private TransactionPersistedEvent createEvent() {
        return new TransactionPersistedEvent(
                "AGT-001",
                "SIM-TXN-001",
                OCCURRED_AT
        );
    }
}
