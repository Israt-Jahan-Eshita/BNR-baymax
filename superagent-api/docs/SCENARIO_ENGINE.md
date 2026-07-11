# Scenario Engine

The Scenario Engine injects controlled synthetic conditions into the backend to validate analytics dynamically.

## Principles
- **Condition Injection:** The scenario injects transactions or data health anomalies, not alerts.
- **Real Execution:** Transactions go through `TransactionService`. Balances change, and `AFTER_COMMIT` analytics discovers signals naturally.
- **Visibility:** Failed runs retain their partially committed synthetic transactions to reflect real system state.

## Scenarios
- `HIDDEN_PROVIDER_SHORTAGE`: Unified visibility of separate provider funds.
- `EVENT_DEMAND_SPIKE`: Contextual, alert-free synthetic event.
- `REPEATED_AMOUNT_CLUSTER`: Tests the deterministic amount cluster anomaly detection.
- `CASH_OUT_VELOCITY_SPIKE`: Tests the velocity detection logic.
- `PROVIDER_FEED_DELAY` & `CONFLICTING_BALANCE_DATA`: Tests provider health tracking and uncertainty fallback.

Scenario Lab proves our intelligence dynamically: we inject the condition, not the alert, and the backend must discover the signal by itself.
