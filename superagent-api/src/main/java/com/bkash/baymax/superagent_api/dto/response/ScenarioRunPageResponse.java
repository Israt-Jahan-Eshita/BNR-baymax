package com.bkash.baymax.superagent_api.dto.response;

import java.util.List;

public record ScenarioRunPageResponse(
        List<ScenarioRunSummaryResponse> runs,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
