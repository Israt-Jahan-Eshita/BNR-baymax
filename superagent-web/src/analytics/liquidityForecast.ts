import { Forecast, ProviderId, Transaction } from "../domain/models";

export function calculateLiquidityForecast(
  providerId: ProviderId,
  currentBalance: number,
  transactions: Transaction[],
  currentTimeMs: number = Date.now()
): Forecast {
  const txs = transactions.filter((t) => t.provider === providerId);

  const getNetOutflowRate = (windowMin: number) => {
    const windowMs = windowMin * 60 * 1000;
    const cutoff = currentTimeMs - windowMs;
    const windowTxs = txs.filter((t) => t.timestampMs >= cutoff);

    let netOutflow = 0;
    for (const tx of windowTxs) {
      if (tx.type === "cash_out") netOutflow += tx.amount;
      if (tx.type === "cash_in") netOutflow -= tx.amount;
    }

    return netOutflow > 0 ? netOutflow / windowMin : 0;
  };

  const rate15 = getNetOutflowRate(15);
  const rate30 = getNetOutflowRate(30);
  const rate60 = getNetOutflowRate(60);

  const weightedRate = rate15 * 0.5 + rate30 * 0.3 + rate60 * 0.2;

  if (weightedRate <= 0) {
    return {
      providerId,
      currentBalance,
      netOutflowRate: 0,
      projectedRunwayMin: 9999,
      expectedShortageTime: null,
      confidence: 90,
      pressure: "STABLE",
    };
  }

  const projectedRunwayMin = Math.max(0, Math.floor(currentBalance / weightedRate));

  let expectedShortageTime: string | null = null;
  let pressure: Forecast["pressure"] = "STABLE";

  if (projectedRunwayMin < 60) {
    pressure = "HIGH PRESSURE";
    const shortageDate = new Date(currentTimeMs + projectedRunwayMin * 60000);
    expectedShortageTime = shortageDate.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit" });
  } else if (projectedRunwayMin < 180) {
    pressure = "MEDIUM PRESSURE";
    const shortageDate = new Date(currentTimeMs + projectedRunwayMin * 60000);
    expectedShortageTime = shortageDate.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit" });
  }

  return {
    providerId,
    currentBalance,
    netOutflowRate: weightedRate,
    projectedRunwayMin,
    expectedShortageTime,
    confidence: 85,
    pressure,
  };
}
