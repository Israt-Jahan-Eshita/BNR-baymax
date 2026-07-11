"use client";

import { useEffect, useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { getValidationMetrics } from "@/lib/api/validation";
import { ValidationMetricsResponse } from "@/domain/models";

export default function ValidationPage() {
  const { t } = useAppContext();
  const [metrics, setMetrics] = useState<ValidationMetricsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadMetrics() {
      try {
        setLoading(true);
        const data = await getValidationMetrics();
        setMetrics(data);
      } catch (err: any) {
        setError(err.message || "Failed to load validation metrics");
      } finally {
        setLoading(false);
      }
    }
    loadMetrics();
  }, []);

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("nav.validation")}</h1>
        <p className="text-sm text-gray-500 mt-1">Measured using synthetic transactions and simulated operational scenarios.</p>
      </div>

      {loading ? (
        <div className="p-8 text-center text-gray-500 neu-inset rounded-xl font-bold">Loading validation metrics...</div>
      ) : error ? (
        <div className="p-8 text-center text-red-500 neu-inset rounded-xl font-bold">{error}</div>
      ) : !metrics ? (
        <div className="p-8 text-center text-gray-500 neu-inset rounded-xl font-bold">No synthetic scenarios have been evaluated yet.</div>
      ) : (
        <>
          <div className="grid grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            <div className="neu-raised p-6">
              <div className="text-sm font-bold text-gray-500 mb-2">Evaluated Scenarios</div>
              <div className="text-2xl font-bold text-gray-800">{metrics.evaluatedScenarioCount}</div>
            </div>
            <div className="neu-raised p-6">
              <div className="text-sm font-bold text-gray-500 mb-2">Accuracy</div>
              <div className="text-2xl font-bold text-gray-800">{(metrics.accuracy * 100).toFixed(1)}%</div>
            </div>
            <div className="neu-raised p-6">
              <div className="text-sm font-bold text-gray-500 mb-2">Precision</div>
              <div className="text-2xl font-bold text-gray-800">{(metrics.precision * 100).toFixed(1)}%</div>
            </div>
            <div className="neu-raised p-6">
              <div className="text-sm font-bold text-gray-500 mb-2">Recall</div>
              <div className="text-2xl font-bold text-gray-800">{(metrics.recall * 100).toFixed(1)}%</div>
            </div>
            <div className="neu-raised p-6">
              <div className="text-sm font-bold text-gray-500 mb-2">False Positive Rate</div>
              <div className="text-2xl font-bold text-gray-800">{(metrics.falsePositiveRate * 100).toFixed(1)}%</div>
            </div>
            <div className="neu-raised p-6">
              <div className="text-sm font-bold text-gray-500 mb-2">Avg Latency</div>
              <div className="text-2xl font-bold text-gray-800">{metrics.averageDetectionLatencyMilliseconds}ms</div>
            </div>
          </div>

          <div className="neu-inset p-8 rounded-xl">
            <h2 className="text-lg font-bold text-gray-800 mb-4">Validation Method</h2>
            <div className="space-y-4 text-sm text-gray-600 leading-relaxed">
              <p>
                <strong>Interpretation:</strong> {metrics.interpretation}
              </p>
              <p>
                <strong>Scope:</strong> {metrics.validationScope}
              </p>
              <p>
                <strong>Time Range:</strong> {new Date(metrics.start).toLocaleString()} to {new Date(metrics.end).toLocaleString()}
              </p>
              <p>
                <strong>Confusion Matrix:</strong><br />
                True Positives: {metrics.truePositiveCount}<br />
                False Positives: {metrics.falsePositiveCount}<br />
                True Negatives: {metrics.trueNegativeCount}<br />
                False Negatives: {metrics.falseNegativeCount}
              </p>
              <div className="mt-6 p-4 bg-yellow-50/50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-900/30 rounded-lg">
                <strong>Important:</strong> These are prototype validation metrics. They do not imply production readiness or real-world accuracy.
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
