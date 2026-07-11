package com.bkash.baymax.superagent_api.event;

import java.time.Instant;

public record TransactionPersistedEvent(

        String agentCode,
        String transactionReference,
        Instant occurredAt

) {

    public TransactionPersistedEvent {
        if (
                agentCode == null
                || agentCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Transaction analytics event requires an agent code"
            );
        }

        if (
                transactionReference == null
                || transactionReference.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Transaction analytics event requires a transaction reference"
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "Transaction analytics event requires an occurrence time"
            );
        }
    }
}
