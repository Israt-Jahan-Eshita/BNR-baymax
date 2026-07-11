package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CasePriority;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;

import java.time.Instant;
import java.util.List;

public record OperationalCaseDetailResponse(
        String caseCode,
        CaseCreationSource creationSource,
        String sourceAlertCode,
        String agentCode,
        String agentDisplayName,
        String providerCode,
        String providerDisplayName,
        CasePriority priority,
        CaseStatus status,
        String title,
        String description,
        String recommendedNextStep,
        Instant openedAt,
        Instant createdAt,
        Instant updatedAt,
        List<CaseAuditEventResponse> auditTrail
) {
}
