package com.bkash.baymax.superagent_api.dto.request;

import com.bkash.baymax.superagent_api.model.enums.CasePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateManualCaseRequest(

        @NotBlank
        String agentCode,

        String providerCode,

        @NotNull
        CasePriority priority,

        @NotBlank
        @Size(max = 250)
        String title,

        @NotBlank
        @Size(max = 2000)
        String description,

        @NotBlank
        @Size(max = 1500)
        String recommendedNextStep,

        @NotBlank
        @Size(max = 150)
        String createdBy
) {
}
