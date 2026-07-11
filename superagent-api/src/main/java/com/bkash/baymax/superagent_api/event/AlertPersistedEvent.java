package com.bkash.baymax.superagent_api.event;

public record AlertPersistedEvent(
        String alertCode
) {
    public AlertPersistedEvent {
        if (alertCode == null || alertCode.isBlank()) {
            throw new IllegalArgumentException("Alert persisted event requires an alert code");
        }
    }
}
