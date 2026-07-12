# BNR Baymax ML v1 — Commit and Training Guide

## Scope

This folder contains the complete standalone ML v1 work:

- reproducible synthetic dataset generation
- synthetic event calendar
- raw transactions
- 15-minute feature engineering
- scenario ground truth
- leakage-safe train/validation/test splits
- Random Forest training
- validation threshold selection
- holdout evaluation
- saved preprocessing + model pipeline
- evaluation reports

The model predicts `requires_review`.

It does not predict or confirm fraud.

## Repository placement

Place this folder at:

BNR-baymax/
└── bnr-baymax-ml/

Do not place it inside `superagent-api` or `superagent-web`.

## Local training

From the repository root:

```powershell
cd bnr-baymax-ml
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
python src/train_model.py
cd ..
```

## Expected baseline v1 result

The controlled synthetic v1 dataset currently produces unusually strong holdout
results. Treat this as pipeline validation, not production-readiness evidence.

## Commit

```bash
git add bnr-baymax-ml
git status
git diff --cached --stat
git commit -m "Add and train baseline event-aware review signal model"
git push
```

## Runtime limitation

The saved model is not yet called by Spring Boot.

`anomaly_review_pipeline.joblib` is a Python/scikit-learn artifact. A later ML
inference integration step is required to make backend transactions invoke the
trained model dynamically.
