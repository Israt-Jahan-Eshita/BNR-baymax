"use client";

import { useEffect, useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation, DEMO_AGENT_CODE } from "@/context/SimulationContext";
import { getAlerts, getAlertDetail } from "@/lib/api/alerts";
import { AlertSummary, AlertDetail } from "@/domain/models";

function IconAlertTriangle() {
  return <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>;
}

export default function AlertsPage() {
  const { t } = useAppContext();
  const { refreshCounter } = useSimulation();
  
  const [alerts, setAlerts] = useState<AlertSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [selectedAlertCode, setSelectedAlertCode] = useState<string | null>(null);
  const [alertDetail, setAlertDetail] = useState<AlertDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await getAlerts(DEMO_AGENT_CODE);
      setAlerts(res.alerts);
    } catch (err: any) {
      setError(err.message || "Failed to load alerts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, [refreshCounter]);

  const handleSelectAlert = async (alertCode: string) => {
    if (selectedAlertCode === alertCode) return;
    setSelectedAlertCode(alertCode);
    setAlertDetail(null);
    setDetailLoading(true);
    try {
      const detail = await getAlertDetail(alertCode);
      setAlertDetail(detail);
    } catch (err: any) {
      console.error(err);
    } finally {
      setDetailLoading(false);
    }
  };

  const getProviderBadgeColor = (provider: string | null) => {
    if (provider === "bKash") return "var(--bkash)";
    if (provider === "Nagad") return "var(--nagad)";
    if (provider === "Rocket") return "var(--rocket)";
    return "#475569";
  };

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="flex justify-between items-start mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("alerts.title")}</h1>
          <p className="text-sm text-gray-500 mt-1">Review deterministic signals and event-aware context.</p>
        </div>
        <button 
          onClick={fetchAlerts}
          className="text-xs font-bold text-gray-500 bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded"
        >
          Refresh
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* ALERTS LIST */}
        <div className="lg:col-span-1 space-y-4">
          {loading ? (
            <div className="p-8 text-center text-gray-500 neu-inset rounded-xl font-bold">Loading persisted alerts...</div>
          ) : error ? (
            <div className="p-8 text-center text-red-500 neu-inset rounded-xl font-bold">{error}</div>
          ) : alerts.length === 0 ? (
            <div className="p-8 text-center text-gray-500 neu-inset rounded-xl font-bold">No persisted alerts yet. Run a synthetic review scenario from Scenario Lab.</div>
          ) : (
            alerts.map(a => (
              <div 
                key={a.alertCode} 
                onClick={() => handleSelectAlert(a.alertCode)}
                className={`p-4 rounded-xl cursor-pointer transition-colors ${selectedAlertCode === a.alertCode ? "neu-inset border border-gray-300" : "neu-raised border border-transparent hover:border-gray-200"}`}
              >
                <div className="flex items-center justify-between mb-2">
                  <span className={`badge ${a.severity === "CRITICAL" ? "badge-critical" : a.severity === "HIGH" ? "badge-warning" : "badge-info"}`}>
                    {a.severity}
                  </span>
                  <span className="text-xs text-gray-500 font-bold">{new Date(a.detectedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                </div>
                <div className="font-bold text-gray-800 text-sm mb-1">{a.title}</div>
                <div className="text-[11px] text-gray-500 font-bold">{a.providerDisplayName || "System"}</div>
              </div>
            ))
          )}
        </div>

        {/* ALERT DETAIL */}
        <div className="lg:col-span-2">
          {!selectedAlertCode ? (
            <div className="p-12 text-center text-gray-500 neu-inset rounded-xl font-bold h-full flex items-center justify-center">
              Select an alert to view deterministic evidence.
            </div>
          ) : detailLoading ? (
            <div className="p-12 text-center text-gray-500 neu-inset rounded-xl font-bold h-full flex items-center justify-center">
              Loading alert detail...
            </div>
          ) : alertDetail ? (
            <div className="neu-raised rounded-xl overflow-hidden animate-in">
              {/* Header */}
              <div className="p-6 border-b border-gray-200 dark:border-gray-700/50 flex flex-wrap gap-4 justify-between items-start">
                <div className="flex items-start gap-4">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center neu-inset ${alertDetail.severity === "CRITICAL" || alertDetail.severity === "HIGH" ? "text-red-500" : "text-orange-500"}`}>
                    <IconAlertTriangle />
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      {alertDetail.providerDisplayName && (
                        <span className="badge" style={{ background: getProviderBadgeColor(alertDetail.providerCode), color: "white" }}>{alertDetail.providerDisplayName}</span>
                      )}
                      <span className={`badge ${alertDetail.severity === "CRITICAL" ? "badge-critical" : "badge-warning"}`}>{alertDetail.alertType}</span>
                      <span className="text-xs font-mono text-gray-500">#{alertDetail.alertCode}</span>
                    </div>
                    <h3 className="font-bold text-gray-800 text-xl">{alertDetail.title}</h3>
                  </div>
                </div>
                <div className="text-right">
                   <div className="text-xs text-gray-500 font-bold mb-1">SIGNAL CONFIDENCE <span className="text-gray-800 ml-1">{alertDetail.confidence}</span></div>
                   <div className="text-xs text-gray-500 font-bold">CONFIDENCE SCORE <span className="text-gray-800 ml-1">{alertDetail.confidenceScore}%</span></div>
                </div>
              </div>

              <div className="p-6 grid grid-cols-1 gap-8">
                {/* ML ENRICHMENT */}
                {alertDetail.mlReviewProbability !== null && alertDetail.mlReviewProbability !== undefined && (
                  <div className="p-4 border border-purple-200 bg-purple-50 rounded-xl">
                    <h4 className="text-xs font-bold text-purple-800 uppercase tracking-wider mb-4">Event-Aware Review Model</h4>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                      <div>
                        <div className="text-[10px] font-bold text-purple-600 mb-1 uppercase">Review Signal</div>
                        <div className="font-bold text-purple-900 text-lg">{(alertDetail.mlReviewProbability * 100).toFixed(2)}%</div>
                      </div>
                      {alertDetail.mlSelectedThreshold !== null && alertDetail.mlSelectedThreshold !== undefined && (
                        <div>
                          <div className="text-[10px] font-bold text-purple-600 mb-1 uppercase">Decision Threshold</div>
                          <div className="font-bold text-purple-900 text-lg">{(alertDetail.mlSelectedThreshold * 100).toFixed(2)}%</div>
                        </div>
                      )}
                      <div>
                        <div className="text-[10px] font-bold text-purple-600 mb-1 uppercase">Requires Human Review</div>
                        <div className="font-bold text-purple-900 text-lg">{alertDetail.mlRequiresReview ? "YES" : "NO"}</div>
                      </div>
                      {alertDetail.mlModelVersion && (
                        <div>
                          <div className="text-[10px] font-bold text-purple-600 mb-1 uppercase">Model Version</div>
                          <div className="font-bold text-purple-900 text-lg">{alertDetail.mlModelVersion}</div>
                        </div>
                      )}
                    </div>
                    {alertDetail.eventContextSummary && (
                      <div className="mt-4 pt-4 border-t border-purple-200">
                        <div className="text-[10px] font-bold text-purple-600 mb-1 uppercase">Synthetic Calendar Context</div>
                        <div className="text-sm font-medium text-purple-900">{alertDetail.eventContextSummary}</div>
                      </div>
                    )}
                  </div>
                )}

                {/* DETERMINISTIC EVIDENCE */}
                <div className="space-y-6">
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">DETERMINISTIC EVIDENCE</h4>
                    <ul className="space-y-2 text-sm text-gray-700 font-medium list-disc list-inside">
                      {alertDetail.evidence.map((f, i) => <li key={i}>{f}</li>)}
                    </ul>
                  </div>

                  {alertDetail.possibleNormalExplanation && (
                     <div>
                        <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">POSSIBLE NORMAL EXPLANATION</h4>
                        <div className="text-sm text-gray-700 bg-green-50/50 dark:bg-green-900/10 p-3 rounded-lg border border-green-100 dark:border-green-900/30">
                           {alertDetail.possibleNormalExplanation}
                        </div>
                     </div>
                  )}

                  {alertDetail.uncertainty && (
                     <div>
                        <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">UNCERTAINTY</h4>
                        <div className="text-sm text-gray-700 bg-orange-50/50 dark:bg-orange-900/10 p-3 rounded-lg border border-orange-100 dark:border-orange-900/30">
                           {alertDetail.uncertainty}
                        </div>
                     </div>
                  )}
                  
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">SAFE NEXT STEP</h4>
                    <p className="text-sm text-gray-700">{alertDetail.safeNextStep}</p>
                  </div>

                  {(alertDetail.mlReviewProbability === null || alertDetail.mlReviewProbability === undefined) && (
                    <div className="text-xs font-bold text-gray-400 italic">
                      ML review context unavailable. Deterministic evidence remains active.
                    </div>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
