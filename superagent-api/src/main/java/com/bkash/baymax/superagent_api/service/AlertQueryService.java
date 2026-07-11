package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.AlertDetailResponse;
import com.bkash.baymax.superagent_api.dto.response.AlertPageResponse;
import com.bkash.baymax.superagent_api.dto.response.AlertSummaryResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AlertQueryService {

    private final AgentRepository agentRepository;
    private final AlertRepository alertRepository;
    private final AlertService alertService;

    @Transactional(readOnly = true)
    public AlertPageResponse getAlerts(
            String requestedAgentCode,
            String requestedProviderCode,
            AlertType alertType,
            AlertSeverity severity,
            Pageable pageable
    ) {
        String agentCode =
                normalizeRequiredCode(
                        requestedAgentCode,
                        "Agent code"
                );

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException(
                    "Agent not found: " + agentCode
            );
        }

        String providerCode =
                normalizeOptionalCode(
                        requestedProviderCode
                );

        Page<Alert> alertPage =
                alertRepository.findAlerts(
                        agentCode,
                        providerCode,
                        alertType,
                        severity,
                        pageable
                );

        return new AlertPageResponse(
                alertPage.getContent()
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList(),
                alertPage.getNumber(),
                alertPage.getSize(),
                alertPage.getTotalElements(),
                alertPage.getTotalPages(),
                alertPage.isFirst(),
                alertPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public AlertDetailResponse getAlert(
            String requestedAlertCode
    ) {
        String alertCode =
                normalizeRequiredCode(
                        requestedAlertCode,
                        "Alert code"
                );

        Alert alert = alertRepository
                .findByAlertCode(alertCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Alert not found: " + alertCode
                        )
                );

        return alertService.toDetailResponse(alert);
    }

    private AlertSummaryResponse toSummaryResponse(Alert alert) {
        return new AlertSummaryResponse(
                alert.getAlertCode(),
                alert.getAgent().getAgentCode(),
                alert.getProvider() != null ? alert.getProvider().getProviderCode() : null,
                alert.getProvider() != null ? alert.getProvider().getDisplayName() : null,
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getConfidence(),
                alert.getConfidenceScore(),
                alert.getTitle(),
                alert.getSummary(),
                alert.getMlReviewProbability(),
                alert.getMlRequiresReview(),
                alert.getMlModelVersion(),
                alert.getDetectedAt()
        );
    }

    private String normalizeRequiredCode(String code, String fieldName) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
