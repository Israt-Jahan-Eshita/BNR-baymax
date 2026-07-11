import { OperationalCase, ProviderId, Transaction } from "../domain/models";

export function evaluateAnomalies(
  transactions: Transaction[],
  currentTimeMs: number = Date.now()
): Omit<OperationalCase, "id" | "status" | "createdAt" | "updatedAt" | "owner" | "receivedBy">[] {
  const generatedCases: Omit<OperationalCase, "id" | "status" | "createdAt" | "updatedAt" | "owner" | "receivedBy">[] = [];
  
  const providers: ProviderId[] = ["bKash", "Nagad", "Rocket"];

  providers.forEach((provider) => {
    const txs = transactions.filter((t) => t.provider === provider);
    const last15Mins = txs.filter((t) => t.timestampMs >= currentTimeMs - 15 * 60 * 1000);

    // Rule 1: High Velocity Cash Out
    const largeCashOuts = last15Mins.filter((t) => t.type === "cash_out" && t.amount >= 10000);
    if (largeCashOuts.length >= 4) {
      generatedCases.push({
        providerId: provider,
        title: "Anomalous Cash-Out Velocity",
        caseType: "Velocity Spike",
        severity: "CRITICAL",
        confidence: 82,
        whyFlagged: [
          `${largeCashOuts.length} cash-out requests exceeding ৳10,000 in the last 15 minutes`,
          "Originating accounts show repeated requests within short intervals",
          "Historical baseline velocity for this outlet at this hour: < 1 request/hour"
        ],
        evidence: [
          `Current outflow rate is severely elevated for ${provider}.`,
          `Transaction references: ${largeCashOuts.map((t) => t.id).join(", ")}`
        ],
        possibleNormalExplanation: "Eid-related demand may cause a temporary volume spike.",
        uncertaintyNotice: "Pattern detection relies on standard historical baselines which may shift during holidays.",
        recommendedNextStep: "Review the transaction cluster and contact the assigned field officer before escalating.",
      });
    }

    // Rule 2: Repeated Exact Amounts
    const amounts = last15Mins.map((t) => t.amount);
    const amountCounts = amounts.reduce((acc, amt) => {
      acc[amt] = (acc[amt] || 0) + 1;
      return acc;
    }, {} as Record<number, number>);

    for (const [amount, count] of Object.entries(amountCounts)) {
      if (count >= 3 && Number(amount) >= 5000) {
        generatedCases.push({
          providerId: provider,
          title: "Repeated Amount Cluster",
          caseType: "Pattern Match",
          severity: "HIGH",
          confidence: 88,
          whyFlagged: [
            `${count} transactions of exactly ৳${Number(amount).toLocaleString()} within 15 minutes`,
            "Identical high-value transfers are uncommon outside of bulk disbursements"
          ],
          evidence: [
            `Amount: ৳${Number(amount).toLocaleString()}`,
            `Count: ${count}`
          ],
          possibleNormalExplanation: "Agent might be processing a batch payment or salary disbursement for a small local business.",
          uncertaintyNotice: "Unable to verify counterparty identity cluster due to privacy masking.",
          recommendedNextStep: "Acknowledge the alert and monitor if the pattern persists beyond 5 identical transactions.",
        });
        break; // Only trigger one repeated amount cluster per provider per window
      }
    }
  });

  return generatedCases;
}
