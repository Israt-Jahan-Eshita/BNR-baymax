package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.CaseAuditEventResponse;
import com.bkash.baymax.superagent_api.model.CaseAuditEvent;
import com.bkash.baymax.superagent_api.model.OperationalCase;
import com.bkash.baymax.superagent_api.model.enums.CaseAuditAction;
import com.bkash.baymax.superagent_api.model.enums.CaseAuditActorType;
import com.bkash.baymax.superagent_api.repository.CaseAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseAuditService {

    private final CaseAuditEventRepository caseAuditEventRepository;
    private final Clock clock;

    @Transactional
    public void recordAutomaticCaseCreation(
            OperationalCase operationalCase,
            String policyReason
    ) {
        CaseAuditEvent event = CaseAuditEvent.builder()
                .operationalCase(operationalCase)
                .action(CaseAuditAction.CASE_CREATED)
                .actorType(CaseAuditActorType.SYSTEM)
                .actorReference("CASE_CREATION_POLICY")
                .details(
                        "Operational case automatically opened from alert "
                                + operationalCase.getSourceAlert().getAlertCode()
                                + ". "
                                + policyReason
                )
                .occurredAt(Instant.now(clock))
                .build();

        caseAuditEventRepository.save(event);
    }

    @Transactional
    public void recordManualCaseCreation(
            OperationalCase operationalCase,
            String createdBy
    ) {
        CaseAuditEvent event = CaseAuditEvent.builder()
                .operationalCase(operationalCase)
                .action(CaseAuditAction.CASE_CREATED)
                .actorType(CaseAuditActorType.HUMAN)
                .actorReference(createdBy)
                .details("Operational case manually opened by an operations user.")
                .occurredAt(Instant.now(clock))
                .build();

        caseAuditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<CaseAuditEventResponse> getAuditTrail(
            String caseCode
    ) {
        List<CaseAuditEvent> events =
                caseAuditEventRepository
                        .findAllByOperationalCaseCaseCodeOrderByOccurredAtAsc(caseCode);

        return events.stream()
                .map(event -> new CaseAuditEventResponse(
                        event.getAction(),
                        event.getActorType(),
                        event.getActorReference(),
                        event.getDetails(),
                        event.getOccurredAt()
                ))
                .toList();
    }
}
