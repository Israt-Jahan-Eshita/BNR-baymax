package com.bkash.baymax.superagent_api.controller;

import com.bkash.baymax.superagent_api.dto.response.TransactionPageResponse;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionQueryService transactionQueryService;

    @GetMapping
    public TransactionPageResponse getTransactions(
            @RequestParam
            String agentCode,

            @RequestParam(required = false)
            String providerCode,

            @RequestParam(required = false)
            TransactionType type,

            @PageableDefault(
                    size = 20,
                    sort = "occurredAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return transactionQueryService.getTransactions(
                agentCode,
                providerCode,
                type,
                pageable
        );
    }
}
