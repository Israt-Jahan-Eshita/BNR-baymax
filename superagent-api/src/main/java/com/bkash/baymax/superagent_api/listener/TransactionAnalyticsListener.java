package com.bkash.baymax.superagent_api.listener;

import com.bkash.baymax.superagent_api.dto.response.AlertDetailResponse;
import com.bkash.baymax.superagent_api.event.TransactionPersistedEvent;
import com.bkash.baymax.superagent_api.service.AnalyticsCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalyticsListener {

    private final AnalyticsCoordinatorService
            analyticsCoordinatorService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onTransactionPersisted(
            TransactionPersistedEvent event
    ) {
        try {
            List<AlertDetailResponse> createdAlerts =
                    analyticsCoordinatorService
                            .evaluateAnomalies(
                                    event.agentCode()
                            );

            log.info(
                    "Completed analytics evaluation after transaction {} for agent {}. Created {} new alerts.",
                    event.transactionReference(),
                    event.agentCode(),
                    createdAlerts.size()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Analytics evaluation failed after committed transaction {} for agent {}. The transaction remains committed and visible.",
                    event.transactionReference(),
                    event.agentCode(),
                    exception
            );
        }
    }
}
