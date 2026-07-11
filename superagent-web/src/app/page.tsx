"use client";

import { useEffect, useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation, DEMO_AGENT_CODE } from "@/context/SimulationContext";
import { getDashboard } from "@/lib/api/dashboard";
import { getValidationMetrics } from "@/lib/api/validation";
import { 
  DashboardAggregateResponse, 
  ValidationMetricsResponse 
} from "@/domain/models";

/* ===== SVG Icons ===== */
function IconCash() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/></svg>;
}
function IconAlertTriangle() {
  return <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>;
}
function IconClock() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>;
}
function IconArrowRight() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>;
}
function IconActivity() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>;
}

const PROVIDER_CONFIG: Record<string, { color: string; bg: string; border: string }> = {
  bKash: { color: "#e2136e", bg: "#fdf2f8", border: "border-l-[#e2136e]" },
  Nagad: { color: "#f26522", bg: "#fff7ed", border: "border-l-[#f26522]" },
  Rocket: { color: "#7c3aed", bg: "#f5f3ff", border: "border-l-[#7c3aed]" },
};

export default function Dashboard() {
  const { t } = useAppContext();
  const { refreshCounter } = useSimulation();
  
  const [currentTime, setCurrentTime] = useState(new Date());
  const [mounted, setMounted] = useState(false);
  const [dashboard, setDashboard] = useState<DashboardAggregateResponse | null>(null);
  const [validation, setValidation] = useState<ValidationMetricsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setMounted(true);
    const interval = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError(null);
        const [dashData, valData] = await Promise.all([
          getDashboard(DEMO_AGENT_CODE),
          getValidationMetrics().catch(() => null) // validation might be empty
        ]);
        setDashboard(dashData);
        setValidation(valData);
      } catch (err: any) {
        setError(err.message || "Backend unavailable. Start the Spring Boot API and retry.");
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [refreshCounter]);

  if (!mounted) return null;

  if (loading) {
    return (
      <div className="p-6 lg:p-8 flex items-center justify-center min-h-[60vh]">
        <div className="neu-inset p-8 rounded-xl flex items-center gap-4 text-gray-500 font-bold">
          <div className="w-6 h-6 border-4 border-gray-300 border-t-gray-500 rounded-full animate-spin"></div>
          Loading operational state...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 lg:p-8 flex items-center justify-center min-h-[60vh]">
        <div className="neu-raised p-8 rounded-xl text-center max-w-md">
          <div className="w-16 h-16 rounded-full neu-inset flex items-center justify-center mx-auto mb-4 text-red-500">
            <IconAlertTriangle />
          </div>
          <h2 className="text-xl font-bold text-gray-800 mb-2">Connection Error</h2>
          <p className="text-gray-500 font-medium mb-6">{error}</p>
          <button onClick={() => window.location.reload()} className="px-6 py-2 rounded-lg neu-raised font-bold text-gray-700 hover:text-gray-900 transition-colors">
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!dashboard) return null;

  const { balances, forecast, dataHealth, recentAlerts, recentCases } = dashboard;
  
  const activeCriticalAlerts = recentAlerts.alerts.filter(a => a.severity === "CRITICAL");

  return (
    <div className="p-6 lg:p-8 max-w-[1600px] mx-auto space-y-8">
      {/* === TOP BAR === */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("dash.title")}</h1>
          <p className="text-sm text-gray-500 mt-1">{t("dash.outlet")}</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 text-xs text-gray-600 px-4 py-2 rounded-lg neu-inset font-bold">
            <div className="live-dot" />
            <span>{t("dash.live")}</span>
            <span className="text-gray-400">|</span>
            <span>{currentTime.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}</span>
          </div>
          <div className="text-xs text-gray-600 px-4 py-2 rounded-lg neu-inset font-bold">
            {dashboard.agentCode}
          </div>
        </div>
      </div>

      {/* CRITICAL ALERT BANNER */}
      {activeCriticalAlerts.length > 0 && (
        <div className="neu-raised border-l-4 overflow-hidden animate-in" style={{ borderLeftColor: "var(--danger)" }}>
          <div className="p-5 flex gap-4">
            <div className="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--danger)" }}>
              <IconAlertTriangle />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-3 mb-2">
                <span className="badge badge-critical">CRITICAL</span>
                {activeCriticalAlerts[0].providerDisplayName && (
                  <span className="badge badge-info">{activeCriticalAlerts[0].providerDisplayName}</span>
                )}
                <span className="text-xs text-gray-500 font-bold flex items-center gap-1">
                  <IconClock /> {new Date(activeCriticalAlerts[0].detectedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                </span>
              </div>
              <p className="text-[15px] text-gray-800 font-medium leading-relaxed mb-3">{activeCriticalAlerts[0].title} - {activeCriticalAlerts[0].summary}</p>
              <div className="flex items-center gap-4 text-xs">
                <a href="/alerts" className="font-bold flex items-center gap-1 hover:underline" style={{ color: "var(--info)" }}>
                  {t("dash.viewInbox")} <IconArrowRight />
                </a>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* DASHBOARD SECTION: SEE */}
      <div>
        <h2 className="text-xl font-bold text-gray-800 mb-4">SEE: Agent Liquidity</h2>
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          
          <div className="neu-raised p-8 flex flex-col justify-between border-l-4 border-l-gray-400">
            <div>
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">{t("dash.physCash")}</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset text-gray-600"><IconCash /></div>
              </div>
              <div className="metric-value text-gray-800">৳{balances.physicalCashBalance.toLocaleString("en-BD")}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">{t("dash.physCashDesc")}</p>
            </div>
          </div>

          <div className="lg:col-span-3 neu-raised p-8">
            <div className="flex items-center justify-between mb-6">
              <div>
                <span className="metric-label">Informational Provider E-Money Total</span>
                <div className="metric-value text-gray-800 mt-2">৳{balances.totalEMoneyBalance.toLocaleString("en-BD")}</div>
                <p className="text-xs text-gray-500 mt-2 font-bold bg-gray-100 dark:bg-gray-800 inline-block px-2 py-1 rounded">Provider funds remain separate.</p>
              </div>
              <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset text-blue-500"><IconActivity /></div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 border-t border-gray-200 dark:border-gray-700/50 pt-6 mt-4">
              {balances.providerBalances.map(pb => {
                const cfg = PROVIDER_CONFIG[pb.providerCode] || { color: "#3b82f6" };
                return (
                  <div key={pb.providerCode} className="neu-inset p-4 rounded-xl">
                    <div className="text-sm font-bold mb-1" style={{ color: cfg.color }}>{pb.providerDisplayName}</div>
                    <div className="text-lg font-bold text-gray-800">৳{pb.eMoneyBalance.toLocaleString("en-BD")}</div>
                  </div>
                );
              })}
            </div>
          </div>

        </div>
      </div>

      {/* DASHBOARD SECTION: PREDICT */}
      <div className="neu-raised p-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="font-bold text-gray-800 text-lg">{t("dash.liqForecast")}</h3>
            <p className="text-xs text-gray-500 mt-1 font-medium">{t("dash.liqForecastDesc")}</p>
          </div>
          <span className="badge badge-info">{t("dash.predictive")}</span>
        </div>
        
        {forecast.forecasts.length === 0 ? (
          <div className="text-sm font-bold text-gray-500 p-4 text-center">Stable / no active depletion forecast.</div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {forecast.forecasts.map(f => {
              const cfg = f.providerCode ? PROVIDER_CONFIG[f.providerCode] : null;
              const color = cfg ? cfg.color : "#4b5563";
              return (
                <div key={f.resourceType} className="p-6 neu-inset rounded-xl border-l-4" style={{ borderLeftColor: color }}>
                  <div className="flex justify-between items-start mb-4">
                    <div className="font-bold text-lg text-gray-800 flex items-center gap-2">
                      {f.resourceDisplayName}
                      {f.dataHealthStatus && f.dataHealthStatus !== "LIVE" && (
                        <span className={`badge text-[9px] px-1 ${f.dataHealthStatus === "DELAYED" ? "badge-warning" : "badge-critical"}`}>{f.dataHealthStatus}</span>
                      )}
                    </div>
                    {f.status === "HIGH_PRESSURE" ? (
                      <span className="badge badge-critical">HIGH PRESSURE</span>
                    ) : f.status === "MEDIUM_PRESSURE" ? (
                      <span className="badge badge-warning">MEDIUM PRESSURE</span>
                    ) : (
                      <span className="badge badge-success">STABLE</span>
                    )}
                  </div>
                  
                  <div className="space-y-4">
                    <div className="flex justify-between">
                      <div>
                        <div className="text-xs text-gray-500 font-bold mb-1">{t("dash.balance")}</div>
                        <div className="text-xl font-bold">৳{f.currentBalance.toLocaleString()}</div>
                      </div>
                      <div className="text-right">
                        <div className="text-xs text-gray-500 font-bold mb-1">Rate / Min</div>
                        <div className="text-sm font-mono text-gray-700">৳{f.weightedConsumptionPerMinute.toLocaleString()}</div>
                      </div>
                    </div>
                    
                    <div className="flex justify-between">
                      <div>
                        <div className="text-xs text-gray-500 font-bold mb-1">{t("dash.runway")}</div>
                        <div className={`font-mono font-bold ${f.projectedRunwayMinutes !== null && f.projectedRunwayMinutes < 60 ? 'text-red-500' : 'text-gray-700'}`}>
                          {f.projectedRunwayMinutes === null ? "Stable / no active depletion forecast" : f.projectedRunwayMinutes > 1000 ? '>24h' : `${Math.floor(f.projectedRunwayMinutes / 60)}h ${f.projectedRunwayMinutes % 60}m`}
                        </div>
                      </div>
                      <div>
                        <div className="text-xs text-gray-500 font-bold mb-1">{t("dash.shortage")}</div>
                        <div className={`font-mono font-bold ${f.estimatedShortageAt ? 'text-red-500' : 'text-gray-700'}`}>
                          {f.estimatedShortageAt ? new Date(f.estimatedShortageAt).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'}) : "--"}
                        </div>
                      </div>
                    </div>
                    
                    <div className="pt-3 border-t border-gray-200 dark:border-gray-700/50">
                      <p className="text-[11px] text-gray-500 leading-tight mb-2">{f.explanation}</p>
                      {f.uncertainty && <p className="text-[11px] text-orange-500 font-bold leading-tight mb-2">Note: {f.uncertainty}</p>}
                      <div className="flex justify-between items-center text-xs">
                        <span className="text-gray-500 font-bold">Forecast Confidence</span>
                        <span className="font-bold text-gray-700">{Math.round(f.confidenceScore)}% ({f.confidence})</span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* DASHBOARD SECTION: DATA HEALTH */}
      <div className="neu-raised p-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="font-bold text-gray-800 text-lg">{t("dash.dataHealth")}</h3>
            <p className="text-xs text-gray-500 mt-1 font-medium">{t("dash.dataHealthDesc")}</p>
          </div>
        </div>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {dataHealth.map(health => {
            const cfg = PROVIDER_CONFIG[health.providerCode] || { color: "#4b5563" };
            return (
              <div key={health.providerCode} className="flex items-center justify-between p-4 neu-inset rounded-xl">
                <span className="font-bold text-sm text-gray-700" style={{ color: cfg.color }}>{health.providerDisplayName}</span>
                {health.status === "LIVE" && <span className="badge badge-success text-[10px]">LIVE</span>}
                {health.status === "DELAYED" && <span className="badge badge-warning text-[10px]">DELAYED</span>}
                {health.status === "MISSING" && <span className="badge badge-critical text-[10px]">MISSING</span>}
                {health.status === "CONFLICTING" && <span className="badge badge-critical text-[10px]">CONFLICTING</span>}
              </div>
            )
          })}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* DASHBOARD SECTION: REVIEW (ALERTS) */}
        <div className="neu-raised p-8">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-gray-800 text-lg">DETECT: Recent Alerts</h3>
            <a href="/alerts" className="text-xs font-bold text-blue-500 hover:underline">View All</a>
          </div>
          {recentAlerts.alerts.length === 0 ? (
            <div className="text-sm font-bold text-gray-500 p-4 text-center neu-inset rounded-lg">No persisted alerts yet. Run a synthetic review scenario from Scenario Lab.</div>
          ) : (
            <div className="space-y-4">
              {recentAlerts.alerts.slice(0, 5).map(alert => (
                <div key={alert.alertCode} className="p-4 neu-inset rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className={`badge ${alert.severity === 'CRITICAL' ? 'badge-critical' : alert.severity === 'HIGH' ? 'badge-warning' : 'badge-info'}`}>
                      {alert.severity}
                    </span>
                    <span className="text-xs text-gray-500 font-bold">{new Date(alert.detectedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                  </div>
                  <div className="font-bold text-gray-800 text-sm mb-1">{alert.title}</div>
                  <div className="flex justify-between items-end">
                    <div className="text-[11px] text-gray-500 font-bold">{alert.providerDisplayName || 'System'}</div>
                    {alert.mlReviewProbability !== undefined && alert.mlReviewProbability !== null && (
                      <div className="text-[10px] text-purple-600 font-bold">ML Review Signal: {(alert.mlReviewProbability * 100).toFixed(2)}%</div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* DASHBOARD SECTION: ACT (CASES) */}
        <div className="neu-raised p-8">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-gray-800 text-lg">ACT: Operational Cases</h3>
            <a href="/cases" className="text-xs font-bold text-blue-500 hover:underline">View All</a>
          </div>
          {recentCases.cases.length === 0 ? (
            <div className="text-sm font-bold text-gray-500 p-4 text-center neu-inset rounded-lg">No operational cases are currently available.</div>
          ) : (
            <div className="space-y-4">
              {recentCases.cases.slice(0, 5).map(c => (
                <div key={c.caseCode} className="p-4 neu-inset rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-mono text-xs font-bold text-gray-500">{c.caseCode}</span>
                    <span className={`badge ${c.status === 'OPEN' ? 'badge-info' : 'badge-success'}`}>
                      {c.status}
                    </span>
                  </div>
                  <div className="font-bold text-gray-800 text-sm mb-1">{c.title}</div>
                  <div className="text-[11px] text-gray-500 font-bold">Source: {c.creationSource}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* DASHBOARD SECTION: VALIDATE */}
      <div className="neu-raised p-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="font-bold text-gray-800 text-lg">Synthetic Scenario Validation</h3>
            <p className="text-xs text-gray-500 mt-1 font-medium">Measurement of backend signal detection against controlled ground-truth conditions.</p>
          </div>
        </div>
        {!validation || validation.evaluatedScenarioCount === 0 ? (
          <div className="text-sm font-bold text-gray-500 p-4 text-center neu-inset rounded-lg">No synthetic scenarios have been evaluated yet.</div>
        ) : (
          <div>
            <div className="grid grid-cols-2 lg:grid-cols-6 gap-4 mb-6">
              <div className="p-4 neu-inset rounded-lg text-center">
                <div className="text-xs text-gray-500 font-bold mb-1">Evaluated</div>
                <div className="text-xl font-bold text-gray-800">{validation.evaluatedScenarioCount}</div>
              </div>
              <div className="p-4 neu-inset rounded-lg text-center">
                <div className="text-xs text-gray-500 font-bold mb-1">Precision</div>
                <div className="text-xl font-bold text-gray-800">{(validation.precision * 100).toFixed(1)}%</div>
              </div>
              <div className="p-4 neu-inset rounded-lg text-center">
                <div className="text-xs text-gray-500 font-bold mb-1">Recall</div>
                <div className="text-xl font-bold text-gray-800">{(validation.recall * 100).toFixed(1)}%</div>
              </div>
              <div className="p-4 neu-inset rounded-lg text-center">
                <div className="text-xs text-gray-500 font-bold mb-1">FPR</div>
                <div className="text-xl font-bold text-gray-800">{(validation.falsePositiveRate * 100).toFixed(1)}%</div>
              </div>
              <div className="p-4 neu-inset rounded-lg text-center">
                <div className="text-xs text-gray-500 font-bold mb-1">Accuracy</div>
                <div className="text-xl font-bold text-gray-800">{(validation.accuracy * 100).toFixed(1)}%</div>
              </div>
              <div className="p-4 neu-inset rounded-lg text-center">
                <div className="text-xs text-gray-500 font-bold mb-1">Avg Latency</div>
                <div className="text-xl font-bold text-gray-800">{validation.averageDetectionLatencyMilliseconds}ms</div>
              </div>
            </div>
            <div className="p-4 neu-inset rounded-lg text-sm text-gray-600 font-medium leading-relaxed">
              <strong>Interpretation:</strong> {validation.interpretation} <br/>
              <strong>Scope:</strong> {validation.validationScope}
            </div>
          </div>
        )}
      </div>

    </div>
  );
}
