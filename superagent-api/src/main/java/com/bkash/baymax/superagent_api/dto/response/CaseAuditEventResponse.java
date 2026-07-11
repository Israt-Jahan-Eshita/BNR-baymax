package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.CaseAuditAction;
import com.bkash.baymax.superagent_api.model.enums.CaseAuditActorType;

import java.time.Instant;

public record CaseAuditEventResponse(
        CaseAuditAction action,
        CaseAuditActorType actorType,
        String actorReference,
        String details,
        Instant occurredAt
) {
}
