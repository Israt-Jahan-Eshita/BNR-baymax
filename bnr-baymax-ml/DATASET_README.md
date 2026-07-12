# BNR Baymax Synthetic AI Training Dataset

## Target

The model target is `requires_review`.

- `0`: normal or contextually explainable transaction activity
- `1`: an unusual transaction pattern requiring human review

This is **not a fraud label**.

## Statistics

- Seed: 42
- Agents: 20
- Providers: 3
- Synthetic calendar events: 18
- Raw transactions: 45,975
- 15-minute feature windows: 6,176
- Review-positive windows: 377
- Review-negative windows: 5,799

## Event context

The synthetic calendar contains contextual demand windows for synthetic Eid,
Durga Puja, Christmas, salary periods, a local trade fair, and a university
event. Dates are synthetic demo dates, not an official holiday calendar.

`EVENT_DEMAND_SPIKE` is a contextual negative example. High volume during an
event does not automatically require review.

`EVENT_REPEATED_AMOUNT_CLUSTER` is a positive example. Event context does not
cancel strong repeated-amount and account-concentration evidence.

## Files

- `data/raw/transactions.csv`
- `data/raw/event_calendar.csv`
- `data/raw/scenario_ground_truth.csv`
- `data/processed/window_features_labeled.csv`
- `data/splits/train.csv`
- `data/splits/validation.csv`
- `data/splits/test.csv`
- `model_feature_schema.json`
- `data_dictionary.csv`

## Leakage rule

Use only `model_features` from `model_feature_schema.json`.

Never train on `scenario_type`, `scenario_run_id`, `event_name`, `window_id`,
`split_group`, `window_date`, or `split`.

## Recommended first model

Use `RandomForestClassifier(class_weight="balanced")`.

Evaluate precision, recall, F1, confusion matrix, false-positive rate, and
scenario-specific recall.

The model must be described as a synthetic human-review prioritization model,
not as a production fraud detector.
