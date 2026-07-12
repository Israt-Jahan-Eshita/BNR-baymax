
from pathlib import Path
from datetime import datetime, date, timedelta, timezone
import json, math, random
import numpy as np
import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw"
PROCESSED = ROOT / "data" / "processed"
SPLITS = ROOT / "data" / "splits"
REPORTS = ROOT / "reports"
for p in [RAW, PROCESSED, SPLITS, REPORTS]:
    p.mkdir(parents=True, exist_ok=True)

SEED = 42
rng = np.random.default_rng(SEED)
random.seed(SEED)

PROFILES = ["MARKET", "RESIDENTIAL", "UNIVERSITY", "RURAL", "COMMERCIAL"]
REGIONS = ["SYLHET", "DHAKA", "CHATTOGRAM", "RAJSHAHI", "KHULNA"]
PROFILE_RATE = {"MARKET": 4.8, "RESIDENTIAL": 2.5, "UNIVERSITY": 3.8, "RURAL": 1.5, "COMMERCIAL": 5.5}
PROFILE_MEDIAN = {"MARKET": 2400, "RESIDENTIAL": 1600, "UNIVERSITY": 1400, "RURAL": 1200, "COMMERCIAL": 3200}
PROVIDER_MULT = {"BKASH": 1.25, "NAGAD": 1.0, "ROCKET": 0.7}

agents = []
for i in range(20):
    profile = PROFILES[i % len(PROFILES)]
    agents.append({
        "agent_code": f"AGT-{i+1:03d}",
        "display_name": f"Synthetic Agent {i+1:03d}",
        "agent_profile": profile,
        "region": REGIONS[i % len(REGIONS)],
        "normal_15m_rate": PROFILE_RATE[profile],
        "amount_median_bdt": PROFILE_MEDIAN[profile],
    })

providers = [
    {"provider_code": "BKASH", "display_name": "bKash", "base_activity_multiplier": 1.25},
    {"provider_code": "NAGAD", "display_name": "Nagad", "base_activity_multiplier": 1.0},
    {"provider_code": "ROCKET", "display_name": "Rocket", "base_activity_multiplier": 0.7},
]

events = []
def add_event(event_id, event_type, name, start, end, region, multiplier, affected_type):
    events.append({
        "event_id": event_id,
        "event_type": event_type,
        "event_name": name,
        "start_date": pd.Timestamp(start),
        "end_date": pd.Timestamp(end),
        "affected_region": region,
        "expected_demand_multiplier": multiplier,
        "affected_transaction_type": affected_type,
        "calendar_is_synthetic": True,
    })

add_event("EVT-001", "RELIGIOUS_FESTIVAL", "Synthetic Eid Period A", "2026-03-18", "2026-03-22", "ALL", 2.8, "CASH_OUT")
add_event("EVT-002", "RELIGIOUS_FESTIVAL", "Synthetic Eid Period B", "2026-05-25", "2026-05-29", "ALL", 2.4, "CASH_OUT")
add_event("EVT-003", "RELIGIOUS_FESTIVAL", "Synthetic Durga Puja Period", "2026-10-18", "2026-10-22", "ALL", 2.1, "CASH_OUT")
add_event("EVT-004", "RELIGIOUS_FESTIVAL", "Synthetic Christmas Period", "2026-12-23", "2026-12-26", "ALL", 1.6, "CASH_OUT")
add_event("EVT-005", "LOCAL_EVENT", "Synthetic Sylhet Trade Fair", "2026-08-10", "2026-08-12", "SYLHET", 1.8, "MIXED")
add_event("EVT-006", "UNIVERSITY_EVENT", "Synthetic Admission Week", "2026-01-15", "2026-01-18", "SYLHET", 1.7, "MIXED")
for month in range(1, 13):
    start = date(2026, month, 28)
    end = min(start + timedelta(days=5), date(2026, 12, 31))
    add_event(f"EVT-SAL-{month:02d}", "SALARY_PERIOD", f"Synthetic Salary Window {month:02d}", start.isoformat(), end.isoformat(), "ALL", 1.8, "MIXED")

def context_for(ts, region):
    d = pd.Timestamp(ts).tz_localize(None).normalize() if pd.Timestamp(ts).tzinfo else pd.Timestamp(ts).normalize()
    applicable = [e for e in events if e["affected_region"] in ("ALL", region)]
    active = [e for e in applicable if e["start_date"] <= d <= e["end_date"]]
    if active:
        e = max(active, key=lambda x: x["expected_demand_multiplier"])
        return {
            "is_event_active": 1,
            "recent_event_within_3d": 1,
            "event_type": e["event_type"],
            "event_name": e["event_name"],
            "event_demand_multiplier": float(e["expected_demand_multiplier"]),
            "event_proximity_days": 0,
        }
    nearest = min(
        applicable,
        key=lambda e: min(abs((d - e["start_date"]).days), abs((d - e["end_date"]).days))
    )
    dist = min(abs((d - nearest["start_date"]).days), abs((d - nearest["end_date"]).days))
    if dist <= 3:
        return {
            "is_event_active": 0,
            "recent_event_within_3d": 1,
            "event_type": nearest["event_type"],
            "event_name": nearest["event_name"],
            "event_demand_multiplier": round(float(nearest["expected_demand_multiplier"]) * 0.75, 3),
            "event_proximity_days": int(dist),
        }
    return {
        "is_event_active": 0,
        "recent_event_within_3d": 0,
        "event_type": "NONE",
        "event_name": "NONE",
        "event_demand_multiplier": 1.0,
        "event_proximity_days": min(int(dist), 30),
    }

def hour_mult(hour):
    if 8 <= hour < 10: return 0.7
    if 10 <= hour < 14: return 1.25
    if 14 <= hour < 17: return 0.9
    if 17 <= hour < 21: return 1.3
    return 0.5

def day_mult(ts):
    return 0.9 if ts.dayofweek in [4, 5] else 1.0

def amount(profile, event_mult=1.0, center=None):
    if center is not None:
        value = center * float(rng.uniform(0.98, 1.02))
    else:
        value = float(rng.lognormal(math.log(PROFILE_MEDIAN[profile]), 0.65))
        if event_mult > 1:
            value *= float(rng.uniform(1.0, min(1.3, 1 + 0.1 * event_mult)))
    return round(min(max(value, 100), 50000) / 10) * 10

# Representative days: every contextual day plus normal samples.
event_days = set()
for e in events:
    for d in pd.date_range(e["start_date"] - pd.Timedelta(days=3), e["end_date"] + pd.Timedelta(days=3), freq="D"):
        event_days.add(pd.Timestamp(d).normalize())
all_days = list(pd.date_range("2026-01-01", "2026-12-31", freq="D"))
normal_days = [d for d in all_days if d.normalize() not in event_days]
sampled_normal = [pd.Timestamp(x).normalize() for x in rng.choice(normal_days, size=45, replace=False)]
selected_days = sorted(event_days.union(sampled_normal))
if len(selected_days) > 100:
    active_days = set()
    for e in events:
        active_days.update(pd.date_range(e["start_date"], e["end_date"], freq="D").normalize())
    remaining = [d for d in selected_days if d not in active_days]
    need = max(0, 100 - len(active_days))
    selected_days = sorted(active_days.union(pd.Timestamp(x).normalize() for x in rng.choice(remaining, size=need, replace=False)))

time_slots = [(h, m) for h in range(8, 22) for m in (0, 15, 30, 45)]
transactions = []
tx_no = 1

# Normal and event-driven activity.
for d in selected_days:
    for agent in agents:
        for provider_code in PROVIDER_MULT:
            hour, minute = time_slots[int(rng.integers(0, len(time_slots)))]
            ws = pd.Timestamp(datetime(d.year, d.month, d.day, hour, minute, tzinfo=timezone.utc))
            ctx = context_for(ws, agent["region"])
            normal_lambda = PROFILE_RATE[agent["agent_profile"]] * PROVIDER_MULT[provider_code] * hour_mult(hour) * day_mult(ws)
            observed_lambda = normal_lambda * ctx["event_demand_multiplier"] if ctx["is_event_active"] else normal_lambda
            if ctx["recent_event_within_3d"] and not ctx["is_event_active"]:
                observed_lambda *= max(1.0, ctx["event_demand_multiplier"])
            count = int(rng.poisson(max(observed_lambda, 0.1)))
            for _ in range(count):
                p_out = 0.68 if ctx["event_type"] in ("RELIGIOUS_FESTIVAL", "SALARY_PERIOD") else 0.55
                tx_type = "CASH_OUT" if rng.random() < p_out else "CASH_IN"
                occurred = ws + pd.Timedelta(seconds=int(rng.integers(0, 900)))
                transactions.append({
                    "transaction_reference": f"TX-{tx_no:07d}",
                    "agent_code": agent["agent_code"],
                    "agent_profile": agent["agent_profile"],
                    "region": agent["region"],
                    "provider_code": provider_code,
                    "transaction_type": tx_type,
                    "amount": amount(agent["agent_profile"], ctx["event_demand_multiplier"]),
                    "occurred_at": occurred.isoformat(),
                    "synthetic_account_id": f"SIM-{agent['agent_code']}-{int(rng.integers(1, 501)):04d}",
                    "source": "BASELINE",
                    "scenario_run_id": "",
                    "scenario_type": "EVENT_DEMAND_SPIKE" if ctx["is_event_active"] and observed_lambda >= normal_lambda * 1.5 else "NORMAL",
                })
                tx_no += 1

active_event_days = [d for d in selected_days if any(e["start_date"] <= d <= e["end_date"] for e in events)]
non_event_days = [d for d in selected_days if not any(e["start_date"] <= d <= e["end_date"] for e in events)]
ground_truth = []
run_no = 1

def inject(kind, event_required=False):
    global tx_no, run_no
    agent = agents[int(rng.integers(0, len(agents)))]
    provider_code = providers[int(rng.integers(0, len(providers)))]["provider_code"]
    pool = active_event_days if event_required else non_event_days
    d = pool[int(rng.integers(0, len(pool)))]
    hour, minute = time_slots[int(rng.integers(0, len(time_slots)))]
    ws = pd.Timestamp(datetime(d.year, d.month, d.day, hour, minute, tzinfo=timezone.utc))
    ctx = context_for(ws, agent["region"])
    run_id = f"RUN-{run_no:05d}"
    run_no += 1

    if kind == "CASH_OUT_VELOCITY_SPIKE":
        n = int(rng.integers(22, 34))
        ids = rng.choice(np.arange(1, 501), size=min(n, 20), replace=False)
        accounts = [f"SIM-{agent['agent_code']}-{int(x):04d}" for x in ids]
        for i in range(n):
            transactions.append({
                "transaction_reference": f"TX-{tx_no:07d}",
                "agent_code": agent["agent_code"],
                "agent_profile": agent["agent_profile"],
                "region": agent["region"],
                "provider_code": provider_code,
                "transaction_type": "CASH_OUT",
                "amount": amount(agent["agent_profile"]),
                "occurred_at": (ws + pd.Timedelta(seconds=int(rng.integers(0, 900)))).isoformat(),
                "synthetic_account_id": accounts[i % len(accounts)],
                "source": "SCENARIO",
                "scenario_run_id": run_id,
                "scenario_type": kind,
            })
            tx_no += 1
        signal = "High cash-out velocity and volume compared with expected normal activity."
    else:
        n = int(rng.integers(7, 13))
        center = float(rng.choice([5000, 7500, 10000, 12500, 15000]))
        account_count = int(rng.integers(2, 5))
        ids = rng.choice(np.arange(1, 501), size=account_count, replace=False)
        accounts = [f"SIM-{agent['agent_code']}-{int(x):04d}" for x in ids]
        for i in range(n):
            transactions.append({
                "transaction_reference": f"TX-{tx_no:07d}",
                "agent_code": agent["agent_code"],
                "agent_profile": agent["agent_profile"],
                "region": agent["region"],
                "provider_code": provider_code,
                "transaction_type": "CASH_OUT",
                "amount": amount(agent["agent_profile"], center=center),
                "occurred_at": (ws + pd.Timedelta(seconds=int(rng.integers(0, 900)))).isoformat(),
                "synthetic_account_id": accounts[i % len(accounts)],
                "source": "SCENARIO",
                "scenario_run_id": run_id,
                "scenario_type": kind,
            })
            tx_no += 1
        signal = "Near-identical transaction amounts concentrated in a small synthetic account group."

    ground_truth.append({
        "scenario_run_id": run_id,
        "scenario_type": kind,
        "agent_code": agent["agent_code"],
        "provider_code": provider_code,
        "window_start": ws.isoformat(),
        "window_end": (ws + pd.Timedelta(minutes=15)).isoformat(),
        "event_type": ctx["event_type"],
        "event_name": ctx["event_name"],
        "requires_review": 1,
        "expected_signal": signal,
    })

for _ in range(150):
    inject("CASH_OUT_VELOCITY_SPIKE")
for _ in range(150):
    inject("REPEATED_AMOUNT_CLUSTER")
for _ in range(80):
    inject("EVENT_REPEATED_AMOUNT_CLUSTER", event_required=True)

tx = pd.DataFrame(transactions)
tx["occurred_at"] = pd.to_datetime(tx["occurred_at"], utc=True)
tx["window_start"] = tx["occurred_at"].dt.floor("15min")
tx = tx.sort_values("occurred_at").reset_index(drop=True)

truth = pd.DataFrame(ground_truth)
truth["window_start"] = pd.to_datetime(truth["window_start"], utc=True)
truth["window_end"] = pd.to_datetime(truth["window_end"], utc=True)

def cluster_stats(values):
    a = np.sort(np.asarray(values, dtype=float))
    if len(a) == 0:
        return 0, 0.0
    best = 1
    for i, x in enumerate(a):
        j = np.searchsorted(a, x * 1.02, side="right")
        best = max(best, j - i)
    return int(best), float(best / len(a))

priority = {"EVENT_REPEATED_AMOUNT_CLUSTER": 3, "REPEATED_AMOUNT_CLUSTER": 2, "CASH_OUT_VELOCITY_SPIKE": 1}
features = []

for keys, g in tx.groupby(["agent_code", "agent_profile", "region", "provider_code", "window_start"], sort=False):
    agent_code, profile, region, provider_code, ws = keys
    cash_in = g[g["transaction_type"] == "CASH_IN"]
    cash_out = g[g["transaction_type"] == "CASH_OUT"]
    near_count, repeated_ratio = cluster_stats(g["amount"].values)
    account_counts = g["synthetic_account_id"].value_counts()
    ctx = context_for(ws, region)
    base_count = PROFILE_RATE[profile] * PROVIDER_MULT[provider_code] * hour_mult(ws.hour) * day_mult(ws)
    expected_mean = PROFILE_MEDIAN[profile] * math.exp((0.65 ** 2) / 2)
    baseline_cash_out = max(base_count * 0.55 * expected_mean, 100.0)

    run_ids = [x for x in g["scenario_run_id"].astype(str).unique() if x]
    matches = truth[truth["scenario_run_id"].isin(run_ids)]
    if len(matches):
        scenario_type = max(matches["scenario_type"], key=lambda x: priority.get(x, 0))
        scenario_run_id = "|".join(sorted(matches["scenario_run_id"].unique()))
        review = 1
    else:
        scenario_type = "EVENT_DEMAND_SPIKE" if ctx["is_event_active"] and len(g) >= base_count * 1.5 else "NORMAL"
        scenario_run_id = ""
        review = 0

    features.append({
        "window_id": f"{agent_code}-{provider_code}-{ws.strftime('%Y%m%dT%H%M')}",
        "agent_code": agent_code,
        "agent_profile": profile,
        "region": region,
        "provider_code": provider_code,
        "window_start": ws.isoformat(),
        "hour_of_day": int(ws.hour),
        "day_of_week": int(ws.dayofweek),
        "is_weekend": int(ws.dayofweek in [4, 5]),
        "transaction_count": int(len(g)),
        "cash_in_count": int(len(cash_in)),
        "cash_out_count": int(len(cash_out)),
        "cash_in_amount": round(float(cash_in["amount"].sum()), 2),
        "cash_out_amount": round(float(cash_out["amount"].sum()), 2),
        "average_amount": round(float(g["amount"].mean()), 2),
        "median_amount": round(float(g["amount"].median()), 2),
        "amount_std_deviation": round(float(g["amount"].std(ddof=0)), 2),
        "unique_account_count": int(g["synthetic_account_id"].nunique()),
        "top_account_share": round(float(account_counts.iloc[0] / len(g)), 4),
        "near_identical_amount_count": near_count,
        "repeated_amount_ratio": round(repeated_ratio, 4),
        "baseline_transaction_count": round(float(base_count), 4),
        "baseline_cash_out_amount": round(float(baseline_cash_out), 2),
        "transaction_count_multiplier": round(float(len(g) / max(base_count, 0.5)), 4),
        "cash_out_amount_multiplier": round(float(cash_out["amount"].sum() / baseline_cash_out), 4),
        "is_event_active": int(ctx["is_event_active"]),
        "recent_event_within_3d": int(ctx["recent_event_within_3d"]),
        "event_type": ctx["event_type"],
        "event_name": ctx["event_name"],
        "event_proximity_days": int(ctx["event_proximity_days"]),
        "event_demand_multiplier": float(ctx["event_demand_multiplier"]),
        "scenario_type": scenario_type,
        "scenario_run_id": scenario_run_id,
        "requires_review": review,
    })

feat = pd.DataFrame(features).sort_values("window_start").reset_index(drop=True)
feat["window_date"] = pd.to_datetime(feat["window_start"], utc=True).dt.date.astype(str)
feat["split_group"] = np.where(feat["scenario_run_id"].str.len() > 0, feat["scenario_run_id"], "DATE-" + feat["window_date"])

groups = list(feat["split_group"].drop_duplicates())
random.Random(SEED).shuffle(groups)
train_cut = int(len(groups) * 0.70)
val_cut = int(len(groups) * 0.85)
train_groups = set(groups[:train_cut])
val_groups = set(groups[train_cut:val_cut])

def split_name(group):
    if group in train_groups: return "train"
    if group in val_groups: return "validation"
    return "test"

feat["split"] = feat["split_group"].map(split_name)

model_features = [
    "agent_profile", "region", "provider_code",
    "hour_of_day", "day_of_week", "is_weekend",
    "transaction_count", "cash_in_count", "cash_out_count",
    "cash_in_amount", "cash_out_amount",
    "average_amount", "median_amount", "amount_std_deviation",
    "unique_account_count", "top_account_share",
    "near_identical_amount_count", "repeated_amount_ratio",
    "baseline_transaction_count", "baseline_cash_out_amount",
    "transaction_count_multiplier", "cash_out_amount_multiplier",
    "is_event_active", "recent_event_within_3d", "event_type",
    "event_proximity_days", "event_demand_multiplier"
]

pd.DataFrame(agents).to_csv(RAW / "agents.csv", index=False)
pd.DataFrame(providers).to_csv(RAW / "providers.csv", index=False)

event_out = pd.DataFrame(events).copy()
event_out["start_date"] = event_out["start_date"].dt.date.astype(str)
event_out["end_date"] = event_out["end_date"].dt.date.astype(str)
event_out.to_csv(RAW / "event_calendar.csv", index=False)

tx.drop(columns=["window_start"]).to_csv(RAW / "transactions.csv", index=False)
truth.to_csv(RAW / "scenario_ground_truth.csv", index=False)
feat.to_csv(PROCESSED / "window_features_labeled.csv", index=False)

for name in ["train", "validation", "test"]:
    feat[feat["split"] == name].to_csv(SPLITS / f"{name}.csv", index=False)

schema = {
    "dataset_name": "BNR Baymax Synthetic Review-Signal Dataset",
    "version": "1.0.0",
    "random_seed": SEED,
    "window_minutes": 15,
    "target": "requires_review",
    "target_meaning": {
        "0": "Normal or contextually explainable activity",
        "1": "Transaction pattern requires human review"
    },
    "model_features": model_features,
    "categorical_features": ["agent_profile", "region", "provider_code", "event_type"],
    "excluded_audit_columns": [
        "window_id", "agent_code", "window_start", "event_name",
        "scenario_type", "scenario_run_id", "split_group", "window_date", "split"
    ],
    "leakage_warning": "Never use scenario_type, scenario_run_id, event_name, window_id, split_group, window_date, or split as model inputs."
}
(ROOT / "model_feature_schema.json").write_text(json.dumps(schema, indent=2), encoding="utf-8")

dictionary = [
    ("transaction_count", "Synthetic transactions in the 15-minute window", "feature"),
    ("cash_out_amount", "Total CASH_OUT amount in BDT", "feature"),
    ("unique_account_count", "Unique synthetic account identifiers", "feature"),
    ("top_account_share", "Largest single-account transaction share", "feature"),
    ("near_identical_amount_count", "Largest amount cluster within about 2 percent", "feature"),
    ("repeated_amount_ratio", "Near-identical cluster size divided by transaction count", "feature"),
    ("transaction_count_multiplier", "Observed count divided by expected normal count", "feature"),
    ("cash_out_amount_multiplier", "Observed CASH_OUT value divided by expected normal value", "feature"),
    ("is_event_active", "Relevant synthetic calendar event is active", "feature"),
    ("recent_event_within_3d", "Within three days of a relevant event window", "feature"),
    ("event_type", "Event context category", "feature"),
    ("event_proximity_days", "Distance to nearest relevant event window, capped at 30", "feature"),
    ("event_demand_multiplier", "Synthetic expected demand multiplier from calendar context", "feature"),
    ("scenario_type", "Audit-only scenario label; exclude from training inputs", "audit"),
    ("scenario_run_id", "Audit-only scenario identifier; exclude from training inputs", "audit"),
    ("requires_review", "Target: 1 requires human review, 0 otherwise", "target"),
]
pd.DataFrame(dictionary, columns=["column", "description", "role"]).to_csv(ROOT / "data_dictionary.csv", index=False)

summary = {
    "random_seed": SEED,
    "agents": len(agents),
    "providers": len(providers),
    "synthetic_calendar_events": len(events),
    "raw_transactions": len(tx),
    "feature_windows": len(feat),
    "review_positive_windows": int(feat["requires_review"].sum()),
    "review_negative_windows": int((feat["requires_review"] == 0).sum()),
    "positive_rate": round(float(feat["requires_review"].mean()), 4),
    "split_counts": {k: int(v) for k, v in feat["split"].value_counts().to_dict().items()},
    "split_positive_rates": {k: float(v) for k, v in feat.groupby("split")["requires_review"].mean().round(4).to_dict().items()},
    "scenario_counts": {k: int(v) for k, v in feat["scenario_type"].value_counts().to_dict().items()}
}
(REPORTS / "dataset_summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")

readme = f"""# BNR Baymax Synthetic AI Training Dataset

## Target

The model target is `requires_review`.

- `0`: normal or contextually explainable transaction activity
- `1`: an unusual transaction pattern requiring human review

This is **not a fraud label**.

## Statistics

- Seed: {SEED}
- Agents: {len(agents)}
- Providers: {len(providers)}
- Synthetic calendar events: {len(events)}
- Raw transactions: {len(tx):,}
- 15-minute feature windows: {len(feat):,}
- Review-positive windows: {int(feat["requires_review"].sum()):,}
- Review-negative windows: {int((feat["requires_review"] == 0).sum()):,}

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
"""
(ROOT / "DATASET_README.md").write_text(readme, encoding="utf-8")

(ROOT / "requirements.txt").write_text(
    "pandas>=2.2\nnumpy>=1.26\nscikit-learn>=1.5\njoblib>=1.4\n",
    encoding="utf-8"
)

print(json.dumps(summary, indent=2))
