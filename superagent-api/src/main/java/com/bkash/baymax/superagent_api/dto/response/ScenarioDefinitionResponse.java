package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.ScenarioType;

public record ScenarioDefinitionResponse(
        ScenarioType scenarioType,
        String displayName,
        String description,
        String expectedSystemEffect
) {
}
