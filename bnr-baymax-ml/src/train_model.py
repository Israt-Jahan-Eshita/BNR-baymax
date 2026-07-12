from pathlib import Path
import json
import joblib
import numpy as np
import pandas as pd

from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder
from sklearn.pipeline import Pipeline
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import precision_score, recall_score, f1_score, confusion_matrix

ROOT = Path(__file__).resolve().parents[1]
DATASET_ROOT = ROOT.parent / "bnr-baymax-ml"

schema = json.loads(
    (DATASET_ROOT / "model_feature_schema.json").read_text(encoding="utf-8")
)

train = pd.read_csv(DATASET_ROOT / "data" / "splits" / "train.csv")
validation = pd.read_csv(DATASET_ROOT / "data" / "splits" / "validation.csv")
test = pd.read_csv(DATASET_ROOT / "data" / "splits" / "test.csv")

features = schema["model_features"]
categorical = schema["categorical_features"]
numeric = [column for column in features if column not in categorical]
target = schema["target"]

preprocessor = ColumnTransformer(
    [
        (
            "categorical",
            OneHotEncoder(handle_unknown="ignore", sparse_output=False),
            categorical,
        ),
        ("numeric", "passthrough", numeric),
    ]
)

pipeline = Pipeline(
    [
        ("preprocessor", preprocessor),
        (
            "model",
            RandomForestClassifier(
                n_estimators=500,
                min_samples_split=4,
                min_samples_leaf=2,
                class_weight="balanced_subsample",
                random_state=42,
                n_jobs=-1,
            ),
        ),
    ]
)

pipeline.fit(train[features], train[target])

validation_probability = pipeline.predict_proba(validation[features])[:, 1]

best = None
for threshold in np.arange(0.20, 0.81, 0.01):
    prediction = (validation_probability >= threshold).astype(int)
    precision = precision_score(validation[target], prediction, zero_division=0)
    recall = recall_score(validation[target], prediction, zero_division=0)
    f1 = f1_score(validation[target], prediction, zero_division=0)

    candidate = {
        "threshold": float(round(threshold, 2)),
        "precision": float(precision),
        "recall": float(recall),
        "f1": float(f1),
    }

    if recall >= 0.90 and (
        best is None
        or candidate["f1"] > best["f1"]
        or (
            candidate["f1"] == best["f1"]
            and candidate["precision"] > best["precision"]
        )
    ):
        best = candidate

if best is None:
    raise RuntimeError("No validation threshold reached the required recall target.")

threshold = best["threshold"]
test_probability = pipeline.predict_proba(test[features])[:, 1]
test_prediction = (test_probability >= threshold).astype(int)

tn, fp, fn, tp = confusion_matrix(
    test[target], test_prediction, labels=[0, 1]
).ravel()

metrics = {
    "threshold": threshold,
    "precision": float(
        precision_score(test[target], test_prediction, zero_division=0)
    ),
    "recall": float(
        recall_score(test[target], test_prediction, zero_division=0)
    ),
    "f1": float(
        f1_score(test[target], test_prediction, zero_division=0)
    ),
    "false_positive_rate": float(fp / (fp + tn)),
    "confusion_matrix": {
        "tn": int(tn),
        "fp": int(fp),
        "fn": int(fn),
        "tp": int(tp),
    },
}

output = ROOT / "models"
output.mkdir(parents=True, exist_ok=True)

joblib.dump(pipeline, output / "anomaly_review_pipeline.joblib")
(output / "training_result.json").write_text(
    json.dumps(metrics, indent=2), encoding="utf-8"
)

print(json.dumps(metrics, indent=2))
