"use client";

import { useAppContext } from "@/context/AppContext";
import { useSimulation } from "@/context/SimulationContext";

export default function ValidationPage() {
  const { t } = useAppContext();
  const { metrics } = useSimulation();

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("nav.validation")}</h1>
        <p className="text-sm text-gray-500 mt-1">Measured using synthetic transactions and simulated operational scenarios.</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div className="neu-raised p-6">
          <div className="text-sm font-bold text-gray-500 mb-2">Shortage Detection Lead Time</div>
          <div className="text-2xl font-bold text-gray-800">{metrics.leadTime}</div>
        </div>
        <div className="neu-raised p-6">
          <div className="text-sm font-bold text-gray-500 mb-2">Anomaly Precision</div>
          <div className="text-2xl font-bold text-gray-800">{metrics.precision}</div>
        </div>
        <div className="neu-raised p-6">
          <div className="text-sm font-bold text-gray-500 mb-2">Anomaly Recall</div>
          <div className="text-2xl font-bold text-gray-800">{metrics.recall}</div>
        </div>
        <div className="neu-raised p-6">
          <div className="text-sm font-bold text-gray-500 mb-2">False Positive Rate</div>
          <div className="text-2xl font-bold text-gray-800">{metrics.fpr}</div>
        </div>
        <div className="neu-raised p-6">
          <div className="text-sm font-bold text-gray-500 mb-2">Alert Explanation Coverage</div>
          <div className="text-2xl font-bold text-gray-800">{metrics.explanationCoverage}</div>
        </div>
        <div className="neu-raised p-6">
          <div className="text-sm font-bold text-gray-500 mb-2">P95 Processing Latency</div>
          <div className="text-2xl font-bold text-gray-800">{metrics.p95}</div>
        </div>
      </div>

      <div className="neu-inset p-8 rounded-xl">
        <h2 className="text-lg font-bold text-gray-800 mb-4">Validation Method</h2>
        <div className="space-y-4 text-sm text-gray-600 leading-relaxed">
          <p>
            <strong>Data Source:</strong> All metrics are derived from a synthetic dataset containing 50,000 generated transactions mirroring typical bKash, Nagad, and Rocket daily distribution patterns.
          </p>
          <p>
            <strong>Scenarios:</strong> 12 distinct operational scenarios were injected, including Eid demand spikes, hidden provider shortages, and repeated amount clusters.
          </p>
          <p>
            <strong>Evaluation:</strong> True positives were defined as correct identification of intentionally injected anomalies within a 15-minute window. False positives were defined as alerts generated during known-normal baseline periods.
          </p>
          <p>
            <strong>Limitations:</strong> The current model assumes deterministic reporting from provider APIs. In a production environment, missing or conflicting data (as simulated in the Provider Data Health module) may increase the False Positive Rate. Processing latency does not account for external network round-trips.
          </p>
          <div className="mt-6 p-4 bg-yellow-50/50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-900/30 rounded-lg">
            <strong>Important:</strong> These are prototype validation metrics. They do not imply production readiness or real-world accuracy.
          </div>
        </div>
      </div>
    </div>
  );
}
