package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.CreateManualCaseRequest;
import com.bkash.baymax.superagent_api.dto.response.CaseAuditEventResponse;
import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.OperationalCase;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CasePriority;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;
import com.bkash.baymax.superagent_api.policy.CaseCreationDecision;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.OperationalCaseRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationalCaseService {

    private final OperationalCaseRepository operationalCaseRepository;
    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final CaseAuditService caseAuditService;
    private final Clock clock;

    @Transactional
    public Optional<OperationalCaseDetailResponse> createFromAlertIfAbsent(
            Alert alert,
            CaseCreationDecision decision
    ) {
        if (!decision.shouldCreateCase()) {
            return Optional.empty();
        }

        if (operationalCaseRepository.existsBySourceAlertId(alert.getId())) {
            return Optional.empty();
        }

        OperationalCase operationalCase = OperationalCase.builder()
                .caseCode(generateCaseCode())
                .creationSource(CaseCreationSource.AUTO_ALERT_POLICY)
                .sourceAlert(alert)
                .agent(alert.getAgent())
                .provider(alert.getProvider())
                .priority(mapPriority(alert.getSeverity()))
                .status(CaseStatus.OPEN)
                .title("Operational review: " + alert.getTitle())
                .description("An operational case was opened from alert "
                        + alert.getAlertCode() + ". " + alert.getSummary())
                .recommendedNextStep(alert.getSafeNextStep())
                .openedAt(Instant.now(clock))
                .build();

        OperationalCase savedCase = operationalCaseRepository.save(operationalCase);

        caseAuditService.recordAutomaticCaseCreation(savedCase, decision.reason());

        return Optional.of(toDetailResponse(savedCase));
    }

    @Transactional
    public OperationalCaseDetailResponse createManualCase(
            CreateManualCaseRequest request
    ) {
        String agentCode = request.agentCode().trim().toUpperCase(Locale.ROOT);
        Agent agent = agentRepository.findByAgentCode(agentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentCode));

        Provider provider = null;
        if (request.providerCode() != null && !request.providerCode().isBlank()) {
            String providerCode = request.providerCode().trim().toUpperCase(Locale.ROOT);
            provider = providerRepository.findByProviderCode(providerCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerCode));
        }

        OperationalCase operationalCase = OperationalCase.builder()
                .caseCode(generateCaseCode())
                .creationSource(CaseCreationSource.MANUAL_OPERATOR)
                .sourceAlert(null)
                .agent(agent)
                .provider(provider)
                .priority(request.priority())
                .status(CaseStatus.OPEN)
                .title(request.title().trim())
                .description(request.description().trim())
                .recommendedNextStep(request.recommendedNextStep().trim())
                .openedAt(Instant.now(clock))
                .build();

        OperationalCase savedCase = operationalCaseRepository.save(operationalCase);

        caseAuditService.recordManualCaseCreation(savedCase, request.createdBy());

        return toDetailResponse(savedCase);
    }

    public OperationalCaseDetailResponse toDetailResponse(
            OperationalCase operationalCase
    ) {
        List<CaseAuditEventResponse> auditTrail =
                caseAuditService.getAuditTrail(operationalCase.getCaseCode());

        return new OperationalCaseDetailResponse(
                operationalCase.getCaseCode(),
                operationalCase.getCreationSource(),
                operationalCase.getSourceAlert() != null ? operationalCase.getSourceAlert().getAlertCode() : null,
                operationalCase.getAgent().getAgentCode(),
                operationalCase.getAgent().getDisplayName(),
                operationalCase.getProvider() != null ? operationalCase.getProvider().getProviderCode() : null,
                operationalCase.getProvider() != null ? operationalCase.getProvider().getDisplayName() : null,
                operationalCase.getPriority(),
                operationalCase.getStatus(),
                operationalCase.getTitle(),
                operationalCase.getDescription(),
                operationalCase.getRecommendedNextStep(),
                operationalCase.getOpenedAt(),
                operationalCase.getCreatedAt(),
                operationalCase.getUpdatedAt(),
                auditTrail
        );
    }

    private String generateCaseCode() {
        return "CASE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private CasePriority mapPriority(com.bkash.baymax.superagent_api.model.enums.AlertSeverity severity) {
        return switch (severity) {
            case LOW -> CasePriority.LOW;
            case MEDIUM -> CasePriority.MEDIUM;
            case HIGH -> CasePriority.HIGH;
            case CRITICAL -> CasePriority.CRITICAL;
        };
    }
}
