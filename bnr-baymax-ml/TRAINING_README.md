# BNR Baymax Trained AI Model v1

This model was trained separately from the Next.js website and Spring Boot API.

## Model purpose

The model predicts whether a **15-minute synthetic transaction window requires human review**.

It does **not** confirm fraud.

## Model

- Algorithm: Random Forest
- Training rows: 4,323
- Validation rows: 811
- Test rows: 1,042
- Decision threshold: 0.20
- Class weighting: balanced_subsample
- Random seed: 42

## Test metrics

- Precision: 1.0000
- Recall: 1.0000
- F1: 1.0000
- False-positive rate: 0.0000
- Event-demand false-positive rate: 0.0000

## Important scenario checks

{
  "CASH_OUT_VELOCITY_SPIKE": 1.0,
  "EVENT_REPEATED_AMOUNT_CLUSTER": 1.0,
  "REPEATED_AMOUNT_CLUSTER": 1.0
}

## Artifact

`models/anomaly_review_pipeline.joblib`

This file includes preprocessing and the trained Random Forest model.

## Safety

Use the output as `review_probability` or `review_signal_confidence`.

Do not present it as `fraud_probability`.

Event context is a feature. It can reduce false positives for contextually
expected demand, but it does not cancel strong repeated-amount or concentration
evidence.

## Next integration step

The Spring Boot backend must reproduce the exact 15-minute feature schema in
`model_feature_schema.json`, then call an inference adapter or model service.

Do not connect the frontend directly to the model.
