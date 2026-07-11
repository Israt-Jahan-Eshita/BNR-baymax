package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.policy.CaseCreationDecision;
import com.bkash.baymax.superagent_api.policy.CaseCreationPolicyService;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CaseCreationCoordinatorService {

    private final AlertRepository alertRepository;
    private final CaseCreationPolicyService caseCreationPolicyService;
    private final OperationalCaseService operationalCaseService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<OperationalCaseDetailResponse> evaluateAlert(
            String requestedAlertCode
    ) {
        String alertCode = requestedAlertCode.trim().toUpperCase(Locale.ROOT);

        Alert alert = alertRepository.findByAlertCode(alertCode)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertCode));

        CaseCreationDecision decision = caseCreationPolicyService.evaluate(alert);

        if (!decision.shouldCreateCase()) {
            return Optional.empty();
        }

        return operationalCaseService.createFromAlertIfAbsent(alert, decision);
    }
}
