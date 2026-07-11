package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.request.CreateManualCaseRequest;
import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.dto.response.OperationalCasePageResponse;
import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;
import com.bkash.baymax.superagent_api.service.OperationalCaseQueryService;
import com.bkash.baymax.superagent_api.service.OperationalCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class OperationalCaseController {

    private final OperationalCaseService operationalCaseService;
    private final OperationalCaseQueryService operationalCaseQueryService;

    @PostMapping("/manual")
    public OperationalCaseDetailResponse createManualCase(
            @Valid @RequestBody CreateManualCaseRequest request
    ) {
        return operationalCaseService.createManualCase(request);
    }

    @GetMapping
    public OperationalCasePageResponse getCases(
            @RequestParam String agentCode,
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CaseCreationSource source,
            @PageableDefault(size = 20, sort = "openedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return operationalCaseQueryService.getCases(
                agentCode,
                providerCode,
                status,
                source,
                pageable
        );
    }

    @GetMapping("/{caseCode}")
    public OperationalCaseDetailResponse getCase(
            @PathVariable String caseCode
    ) {
        return operationalCaseQueryService.getCase(caseCode);
    }
}
