package com.bkash.baymax.superagent_api.listener;

import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.event.AlertPersistedEvent;
import com.bkash.baymax.superagent_api.service.CaseCreationCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertCaseCreationListener {

    private final CaseCreationCoordinatorService caseCreationCoordinatorService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAlertPersisted(AlertPersistedEvent event) {
        try {
            Optional<OperationalCaseDetailResponse> createdCase =
                    caseCreationCoordinatorService.evaluateAlert(event.alertCode());

            if (createdCase.isPresent()) {
                log.info("Case creation policy opened case {} from alert {}.",
                        createdCase.get().caseCode(), event.alertCode());
            } else {
                log.info("Case creation policy kept alert {} as alert-only.", event.alertCode());
            }
        } catch (RuntimeException exception) {
            log.error("Case creation policy failed after committed alert {}. The alert remains persisted and visible.",
                    event.alertCode(), exception);
        }
    }
}
