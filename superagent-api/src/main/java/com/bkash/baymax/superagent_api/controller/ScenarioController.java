package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.ScenarioDefinitionResponse;
import com.bkash.baymax.superagent_api.dto.response.ScenarioRunDetailResponse;
import com.bkash.baymax.superagent_api.dto.response.ScenarioRunPageResponse;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.service.ScenarioQueryService;
import com.bkash.baymax.superagent_api.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;
    private final ScenarioQueryService scenarioQueryService;

    @GetMapping("/definitions")
    public List<ScenarioDefinitionResponse> getDefinitions() {
        return scenarioQueryService.getDefinitions();
    }

    @PostMapping("/{scenarioType}/run")
    public ScenarioRunDetailResponse runScenario(
            @PathVariable ScenarioType scenarioType,
            @RequestParam String agentCode
    ) {
        return scenarioService.runScenario(agentCode, scenarioType);
    }

    @GetMapping("/runs")
    public ScenarioRunPageResponse getRuns(
            @RequestParam String agentCode,
            @RequestParam(required = false) ScenarioType scenarioType,
            @RequestParam(required = false) ScenarioRunStatus status,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return scenarioQueryService.getRuns(agentCode, scenarioType, status, pageable);
    }

    @GetMapping("/runs/{scenarioRunId}")
    public ScenarioRunDetailResponse getRun(
            @PathVariable String scenarioRunId
    ) {
        return scenarioQueryService.getRun(scenarioRunId);
    }
}
