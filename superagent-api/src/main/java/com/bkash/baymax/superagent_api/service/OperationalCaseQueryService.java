package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.dto.response.OperationalCasePageResponse;
import com.bkash.baymax.superagent_api.dto.response.OperationalCaseSummaryResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.OperationalCase;
import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.OperationalCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OperationalCaseQueryService {

    private final AgentRepository agentRepository;
    private final OperationalCaseRepository operationalCaseRepository;
    private final OperationalCaseService operationalCaseService;

    @Transactional(readOnly = true)
    public OperationalCasePageResponse getCases(
            String requestedAgentCode,
            String requestedProviderCode,
            CaseStatus status,
            CaseCreationSource creationSource,
            Pageable pageable
    ) {
        String agentCode = requestedAgentCode.trim().toUpperCase(Locale.ROOT);
        
        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException("Agent not found: " + agentCode);
        }

        String providerCode = null;
        if (requestedProviderCode != null && !requestedProviderCode.isBlank()) {
            providerCode = requestedProviderCode.trim().toUpperCase(Locale.ROOT);
        }

        Page<OperationalCase> casePage = operationalCaseRepository.findCases(
                agentCode,
                providerCode,
                status,
                creationSource,
                pageable
        );

        return new OperationalCasePageResponse(
                casePage.getContent().stream().map(this::toSummaryResponse).toList(),
                casePage.getNumber(),
                casePage.getSize(),
                casePage.getTotalElements(),
                casePage.getTotalPages(),
                casePage.isFirst(),
                casePage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public OperationalCaseDetailResponse getCase(
            String requestedCaseCode
    ) {
        String caseCode = requestedCaseCode.trim().toUpperCase(Locale.ROOT);
        
        OperationalCase operationalCase = operationalCaseRepository.findByCaseCode(caseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Operational case not found: " + caseCode));

        return operationalCaseService.toDetailResponse(operationalCase);
    }

    private OperationalCaseSummaryResponse toSummaryResponse(OperationalCase operationalCase) {
        return new OperationalCaseSummaryResponse(
                operationalCase.getCaseCode(),
                operationalCase.getCreationSource(),
                operationalCase.getSourceAlert() != null ? operationalCase.getSourceAlert().getAlertCode() : null,
                operationalCase.getAgent().getAgentCode(),
                operationalCase.getProvider() != null ? operationalCase.getProvider().getProviderCode() : null,
                operationalCase.getProvider() != null ? operationalCase.getProvider().getDisplayName() : null,
                operationalCase.getPriority(),
                operationalCase.getStatus(),
                operationalCase.getTitle(),
                operationalCase.getOpenedAt()
        );
    }
}
