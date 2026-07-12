package com.bkash.baymax.superagent_api.dto.response;

import java.util.List;

public record BaymaxResponse(
        String answer,
        String confidence,
        List<String> reasoningSteps,
        List<String> evidenceList,
        List<String> actionItems,
        List<String> whatIfProjections
) {
}
