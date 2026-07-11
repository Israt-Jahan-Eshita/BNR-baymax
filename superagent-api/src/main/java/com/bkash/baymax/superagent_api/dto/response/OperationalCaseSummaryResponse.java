package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CasePriority;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;

import java.time.Instant;

public record OperationalCaseSummaryResponse(
        String caseCode,
        CaseCreationSource creationSource,
        String sourceAlertCode,
        String agentCode,
        String providerCode,
        String providerDisplayName,
        CasePriority priority,
        CaseStatus status,
        String title,
        Instant openedAt
) {
}
