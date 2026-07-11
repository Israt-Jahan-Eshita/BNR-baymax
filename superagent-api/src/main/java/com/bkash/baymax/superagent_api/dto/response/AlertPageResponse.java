package com.bkash.baymax.superagent_api.dto.response;

import java.util.List;

public record AlertPageResponse(

        List<AlertSummaryResponse> alerts,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last

) {
}
