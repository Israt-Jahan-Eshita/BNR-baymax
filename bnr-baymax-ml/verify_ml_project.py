"""
BNR Baymax ML Project — Full Verification Script
Parts 4-5, 9-10, 14-17, covering dataset counts, split safety,
artifact loading, and test-row inference.
"""
from pathlib import Path
import json, sys
import pandas as pd
import numpy as np

ROOT = Path(__file__).resolve().parent

# ── helpers ──────────────────────────────────────────────────────────────
def sep(title):
    print(f"\n{'='*70}")
    print(f"  {title}")
    print('='*70)

# ── PART 4: Dataset Summary ───────────────────────────────────────────────
sep("PART 4 — DATASET SUMMARY (calculated from files)")

tx = pd.read_csv(ROOT / "data" / "raw" / "transactions.csv")
agents_df = pd.read_csv(ROOT / "data" / "raw" / "agents.csv")
providers_df = pd.read_csv(ROOT / "data" / "raw" / "providers.csv")
feat = pd.read_csv(ROOT / "data" / "processed" / "window_features_labeled.csv")

print(f"Raw transaction row count : {len(tx):,}")
print(f"Synthetic Agent count     : {len(agents_df)}")
print(f"Provider count            : {len(providers_df)}")
print(f"Provider codes            : {list(providers_df['provider_code'])}")
print(f"Total feature windows     : {len(feat):,}")
neg = int((feat['requires_review'] == 0).sum())
pos = int(feat['requires_review'].sum())
rate = pos / len(feat)
print(f"requires_review = 0       : {neg:,}")
print(f"requires_review = 1       : {pos:,}")
print(f"Positive review rate      : {rate:.4f} ({rate*100:.2f}%)")

# ── PART 5: Scenario Distribution ─────────────────────────────────────────
sep("PART 5 — SCENARIO DISTRIBUTION")
sc = feat['scenario_type'].value_counts()
for k, v in sc.items():
    print(f"  {k:40s} {v:,}")

# Check for EID_DEMAND_SPIKE anywhere in data
eid_check = feat[feat['scenario_type'].str.contains('EID', na=False)]
print(f"\nEID_DEMAND_SPIKE occurrences in dataset: {len(eid_check)}")

# ── PART 9: Split Row Counts ───────────────────────────────────────────────
sep("PART 9 — TRAIN / VALIDATION / TEST SPLIT DISTRIBUTION")
for split_name in ['train', 'validation', 'test']:
    df = pd.read_csv(ROOT / "data" / "splits" / f"{split_name}.csv")
    p = int(df['requires_review'].sum())
    n = int((df['requires_review'] == 0).sum())
    r = p / len(df)
    print(f"\n{split_name.upper()}")
    print(f"  rows     : {len(df):,}")
    print(f"  positive : {p}")
    print(f"  negative : {n:,}")
    print(f"  pos rate : {r:.4f} ({r*100:.2f}%)")

# ── PART 10: Group-Safe Split Verification ────────────────────────────────
sep("PART 10 — GROUP-SAFE SPLIT — scenario_run_id OVERLAP CHECK")
train_df = pd.read_csv(ROOT / "data" / "splits" / "train.csv")
val_df   = pd.read_csv(ROOT / "data" / "splits" / "validation.csv")
test_df  = pd.read_csv(ROOT / "data" / "splits" / "test.csv")

def meaningful_run_ids(df):
    col = df['scenario_run_id'].fillna('').astype(str)
    ids = set()
    for entry in col:
        if entry.strip() == 'nan': continue
        for part in entry.split('|'):
            part = part.strip()
            if part and part not in ('', 'nan'):
                ids.add(part)
    return ids

tr_ids = meaningful_run_ids(train_df)
va_ids = meaningful_run_ids(val_df)
te_ids = meaningful_run_ids(test_df)

tv = tr_ids & va_ids
tt = tr_ids & te_ids
vt = va_ids & te_ids

print(f"Train  meaningful scenario_run_ids: {len(tr_ids)}")
print(f"Val    meaningful scenario_run_ids: {len(va_ids)}")
print(f"Test   meaningful scenario_run_ids: {len(te_ids)}")
print(f"\nTrain and Validation overlap: {len(tv)} ids -> {sorted(tv) if tv else 'NONE'}")
print(f"Train and Test       overlap: {len(tt)} ids -> {sorted(tt) if tt else 'NONE'}")
print(f"Val   and Test       overlap: {len(vt)} ids -> {sorted(vt) if vt else 'NONE'}")

if tv or tt or vt:
    print("\n⚠️  LEAKAGE WARNING: scenario_run_id overlap detected between splits!")
else:
    print("\n✅ Group-safe split verified — zero scenario_run_id overlap between splits.")

# ── PART 14: Model Artifact Loading ───────────────────────────────────────
sep("PART 14 — MODEL ARTIFACT VERIFICATION")
import joblib
from sklearn.pipeline import Pipeline
from sklearn.ensemble import RandomForestClassifier

artifact_path = ROOT / "models" / "anomaly_review_pipeline.joblib"
size_bytes = artifact_path.stat().st_size
print(f"Artifact path  : {artifact_path}")
print(f"File size      : {size_bytes:,} bytes ({size_bytes/1e6:.2f} MB)")

pipeline = joblib.load(artifact_path)
print(f"Loaded type    : {type(pipeline)}")
print(f"Named steps    : {list(pipeline.named_steps.keys())}")

pre = pipeline.named_steps.get('preprocessor')
model = pipeline.named_steps.get('model')
print(f"Preprocessor   : {type(pre).__name__}")
print(f"Classifier     : {type(model).__name__}")

rf = model
print(f"\nRandom Forest config:")
print(f"  n_estimators      : {rf.n_estimators}")
print(f"  min_samples_split : {rf.min_samples_split}")
print(f"  min_samples_leaf  : {rf.min_samples_leaf}")
print(f"  class_weight      : {rf.class_weight}")
print(f"  random_state      : {rf.random_state}")
print(f"  n_jobs            : {rf.n_jobs}")

# ── PART 15: Feature Contract Compatibility ───────────────────────────────
sep("PART 15 — FEATURE CONTRACT COMPATIBILITY CHECK")
schema = json.loads((ROOT / "model_feature_schema.json").read_text(encoding='utf-8'))
model_features = schema['model_features']
print(f"Model feature count       : {len(model_features)}")

test_cols = set(test_df.columns)
missing = [f for f in model_features if f not in test_cols]
print(f"Missing model features in test.csv: {missing if missing else 'NONE'}")

dupes = [f for f in model_features if model_features.count(f) > 1]
print(f"Duplicate schema feature names    : {set(dupes) if dupes else 'NONE'}")

leakage_fields = ['scenario_type','scenario_run_id','event_name','window_id',
                  'split','split_group','window_date','agent_code']
present_leakage = [f for f in leakage_fields if f in model_features]
print(f"Leakage fields in model_features  : {present_leakage if present_leakage else 'NONE — CLEAN'}")

# Quick inference test to confirm pipeline accepts only model_features
sample = test_df[model_features].head(1)
prob = pipeline.predict_proba(sample)[0, 1]
print(f"\nSanity inference on first test row: review_probability = {prob:.6f}")

# ── PARTS 16 & 17: Named Test Row Inference ───────────────────────────────
sep("PART 16 — NORMAL TEST ROW INFERENCE: AGT-001-BKASH-20260118T0815")

metadata = json.loads((ROOT / "models" / "model_metadata.json").read_text(encoding='utf-8'))
threshold = float(metadata['selected_threshold'])
print(f"selected_threshold from model_metadata.json: {threshold}")

wid_normal = "AGT-001-BKASH-20260118T0815"
row_normal = test_df[test_df['window_id'] == wid_normal]
if row_normal.empty:
    print(f"  ⚠️  window_id '{wid_normal}' NOT FOUND in test.csv")
    print(f"  Available window_id sample: {list(test_df['window_id'].head(5))}")
else:
    r = row_normal.iloc[0]
    X = row_normal[model_features]
    prob_n = float(pipeline.predict_proba(X)[0, 1])
    requires = prob_n >= threshold
    print(f"  window_id          : {wid_normal}")
    print(f"  actual requires_review (ground truth) : {int(r['requires_review'])}")
    print(f"  scenario_type (audit context only)    : {r.get('scenario_type', 'N/A')}")
    print(f"  event_type (audit context only)       : {r.get('event_type', 'N/A')}")
    print(f"  review_probability                    : {prob_n:.6f}")
    print(f"  selected_threshold                    : {threshold}")
    print(f"  calculated requiresReview             : {requires}  (prob >= threshold = {prob_n:.6f} >= {threshold})")

sep("PART 17 — POSITIVE TEST ROW INFERENCE: AGT-002-BKASH-20260130T2015")

wid_pos = "AGT-002-BKASH-20260130T2015"
row_pos = test_df[test_df['window_id'] == wid_pos]
if row_pos.empty:
    print(f"  ⚠️  window_id '{wid_pos}' NOT FOUND in test.csv")
    print(f"  Searching all splits...")
    for sname in ['train', 'validation', 'test']:
        sdf = pd.read_csv(ROOT / "data" / "splits" / f"{sname}.csv")
        found = sdf[sdf['window_id'] == wid_pos]
        if not found.empty:
            print(f"  Found in {sname}.csv!")
            r = found.iloc[0]
            X = found[model_features]
            prob_p = float(pipeline.predict_proba(X)[0, 1])
            requires_p = prob_p >= threshold
            print(f"  window_id          : {wid_pos}")
            print(f"  actual requires_review (ground truth) : {int(r['requires_review'])}")
            print(f"  scenario_type (audit context only)    : {r.get('scenario_type', 'N/A')}")
            print(f"  event_type (audit context only)       : {r.get('event_type', 'N/A')}")
            print(f"  review_probability                    : {prob_p:.6f}")
            print(f"  selected_threshold                    : {threshold}")
            print(f"  calculated requiresReview             : {requires_p}")
            break
    else:
        print(f"  NOT FOUND in any split. Checking full labeled dataset...")
        all_feat = pd.read_csv(ROOT / "data" / "processed" / "window_features_labeled.csv")
        found_all = all_feat[all_feat['window_id'] == wid_pos]
        if found_all.empty:
            print(f"  NOT FOUND in window_features_labeled.csv either.")
            # List all window_ids for AGT-002
            agt002 = all_feat[all_feat['window_id'].str.startswith('AGT-002-BKASH')]
            pos_rows = agt002[agt002['requires_review'] == 1]
            print(f"  AGT-002-BKASH positive windows (first 5):")
            print(pos_rows[['window_id','scenario_type','event_type','requires_review']].head().to_string())
        else:
            print(f"  Found in full dataset (split: {found_all.iloc[0].get('split','?')})")
else:
    r = row_pos.iloc[0]
    X = row_pos[model_features]
    prob_p = float(pipeline.predict_proba(X)[0, 1])
    requires_p = prob_p >= threshold
    print(f"  window_id          : {wid_pos}")
    print(f"  actual requires_review (ground truth) : {int(r['requires_review'])}")
    print(f"  scenario_type (audit context only)    : {r.get('scenario_type', 'N/A')}")
    print(f"  event_type (audit context only)       : {r.get('event_type', 'N/A')}")
    print(f"  review_probability                    : {prob_p:.6f}")
    print(f"  selected_threshold                    : {threshold}")
    print(f"  calculated requiresReview             : {requires_p}")

print("\n" + "="*70)
print("  VERIFICATION SCRIPT COMPLETE")
print("="*70)
