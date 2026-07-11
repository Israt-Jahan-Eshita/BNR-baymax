"use client";

import { useEffect, useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation } from "@/context/SimulationContext";
import { ProviderId } from "@/domain/models";

/* ===== SVG Icons ===== */
function IconCash() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/></svg>;
}
function IconTrendUp() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>;
}
function IconTrendDown() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/></svg>;
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
function IconUsers() {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>;
}

const PROVIDER_CONFIG: Record<string, { color: string; bg: string; border: string }> = {
  bKash: { color: "#e2136e", bg: "#fdf2f8", border: "border-l-[#e2136e]" },
  Nagad: { color: "#f26522", bg: "#fff7ed", border: "border-l-[#f26522]" },
  Rocket: { color: "#7c3aed", bg: "#f5f3ff", border: "border-l-[#7c3aed]" },
};

export default function Dashboard() {
  const { t } = useAppContext();
  const { balances, forecasts, cases, providerHealth, transactions } = useSimulation();
  
  const [currentTime, setCurrentTime] = useState(new Date());
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const interval = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(interval);
  }, []);

  const totalEMoney = balances.bKash + balances.Nagad + balances.Rocket;
  const totalCombined = balances.PhysicalCash + totalEMoney;
  
  const activeAlerts = cases.filter(c => c.status === "OPEN" && c.severity === "CRITICAL");
  const txToday = transactions.length;

  const providers: ProviderId[] = ["bKash", "Nagad", "Rocket"];

  return (
    <div className="p-6 lg:p-8 max-w-[1600px] mx-auto">
      {/* === TOP BAR === */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("dash.title")}</h1>
          <p className="text-sm text-gray-500 mt-1">{t("dash.outlet")}</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 text-xs text-gray-600 px-4 py-2 rounded-lg neu-inset font-bold">
            <div className="live-dot" />
            <span>{t("dash.live")}</span>
            <span className="text-gray-400">|</span>
            <span>{mounted ? currentTime.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit", second: "2-digit" }) : "--:--:--"}</span>
          </div>
          <div className="text-xs text-gray-600 px-4 py-2 rounded-lg neu-inset font-bold">
            {t("dash.agentId")}
          </div>
        </div>
      </div>

      <div>
        <div className="space-y-8">
          
          {/* CRITICAL ALERT BANNER */}
          {activeAlerts.length > 0 && (
            <div className="neu-raised border-l-4 overflow-hidden animate-in" style={{ borderLeftColor: "var(--danger)" }}>
              <div className="p-5 flex gap-4">
                <div className="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--danger)" }}>
                  <IconAlertTriangle />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 mb-2">
                    <span className="badge badge-critical">{t("dash.critical")}</span>
                    <span className="badge" style={{ background: "var(--bkash)", color: "white" }}>{activeAlerts[0].providerId}</span>
                    <span className="text-xs text-gray-500 font-bold flex items-center gap-1"><IconClock /> {activeAlerts[0].createdAt.split('T')[1].slice(0,5)}</span>
                  </div>
                  <p className="text-[15px] text-gray-800 font-medium leading-relaxed mb-3">{activeAlerts[0].title} - {activeAlerts[0].whyFlagged[0]}</p>
                  <div className="flex items-center gap-4 text-xs">
                    <a href="/alerts" className="font-bold flex items-center gap-1 hover:underline" style={{ color: "var(--bkash)" }}>
                      {t("dash.viewInbox")} <IconArrowRight />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* SUMMARY METRICS */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-8">
            <div className="neu-raised p-8">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">{t("dash.totLiq")}</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--info)" }}><IconActivity /></div>
              </div>
              <div className="metric-value">৳{totalCombined.toLocaleString("en-BD")}</div>
              <p className="text-[11px] text-gray-400 mt-2 leading-tight">{t("dash.totLiqDesc")}</p>
            </div>

            <div className="neu-raised p-8">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">{t("dash.physCash")}</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--cash)" }}><IconCash /></div>
              </div>
              <div className="metric-value" style={{ color: "var(--cash)" }}>৳{balances.PhysicalCash.toLocaleString("en-BD")}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">{t("dash.physCashDesc")}</p>
            </div>

            <div className="neu-raised p-8">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">{t("dash.totEmoney")}</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--bkash)" }}><IconCash /></div>
              </div>
              <div className="metric-value">৳{totalEMoney.toLocaleString("en-BD")}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">{t("dash.totEmoneyDesc")}</p>
            </div>

            <div className="neu-raised p-8">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">{t("dash.txToday")}</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--warning)" }}><IconUsers /></div>
              </div>
              <div className="metric-value">{txToday}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">{t("dash.txTodayDesc")}</p>
            </div>
          </div>

          {/* LIQUIDITY FORECAST (BNR BAYMAX NEW) */}
          <div className="neu-raised p-8">
             <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="font-bold text-gray-800 text-lg">{t("dash.liqForecast")}</h3>
                  <p className="text-xs text-gray-500 mt-1 font-medium">{t("dash.liqForecastDesc")}</p>
                </div>
                <span className="badge badge-info">{t("dash.predictive")}</span>
              </div>
              
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                 {providers.map(p => {
                    const f = forecasts[p];
                    const cfg = PROVIDER_CONFIG[p] || PROVIDER_CONFIG.bKash;
                    if (!f) return null;
                    return (
                       <div key={p} className="p-6 neu-inset rounded-xl border-l-4" style={{ borderLeftColor: cfg.color }}>
                          <div className="flex justify-between items-start mb-4">
                             <div className="font-bold text-lg text-gray-800 flex items-center gap-2">
                               {p} 
                               {providerHealth[p].state === "DELAYED" && <span className="badge badge-warning text-[9px] px-1">{t("dash.delayed")}</span>}
                             </div>
                             {f.pressure === "HIGH PRESSURE" ? (
                               <span className="badge badge-critical">{t("dash.pressure")}: HIGH</span>
                             ) : f.pressure === "MEDIUM PRESSURE" ? (
                               <span className="badge badge-warning">{t("dash.pressure")}: MEDIUM</span>
                             ) : (
                               <span className="badge badge-success">{t("dash.healthy")}</span>
                             )}
                          </div>
                          
                          <div className="space-y-4">
                             <div>
                                <div className="text-xs text-gray-500 font-bold mb-1">{t("dash.balance")}</div>
                                <div className="text-xl font-bold">৳{f.currentBalance.toLocaleString()}</div>
                             </div>
                             
                             <div className="flex justify-between">
                                <div>
                                   <div className="text-xs text-gray-500 font-bold mb-1">{t("dash.runway")}</div>
                                   <div className={`font-mono font-bold ${f.projectedRunwayMin < 60 ? 'text-red-500' : 'text-gray-700'}`}>
                                      {f.projectedRunwayMin > 1000 ? '>24h' : `${Math.floor(f.projectedRunwayMin / 60)}h ${f.projectedRunwayMin % 60}m`}
                                   </div>
                                </div>
                                <div>
                                   <div className="text-xs text-gray-500 font-bold mb-1">{t("dash.shortage")}</div>
                                   <div className={`font-mono font-bold ${f.expectedShortageTime ? 'text-red-500' : 'text-gray-700'}`}>
                                      {f.expectedShortageTime || "--"}
                                   </div>
                                </div>
                             </div>
                             
                             <div className="pt-3 border-t border-gray-200 dark:border-gray-700/50 flex justify-between items-center text-xs">
                               <span className="text-gray-500 font-bold">Forecast Confidence</span>
                               <span className="font-bold text-gray-700">{Math.round(f.confidence)}%</span>
                             </div>
                          </div>
                       </div>
                    );
                 })}
              </div>
          </div>
          
          {/* PROVIDER DATA HEALTH */}
          <div className="neu-raised p-8">
             <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="font-bold text-gray-800 text-lg">{t("dash.dataHealth")}</h3>
                  <p className="text-xs text-gray-500 mt-1 font-medium">{t("dash.dataHealthDesc")}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                 {providers.map(p => {
                    const health = providerHealth[p];
                    const cfg = PROVIDER_CONFIG[p] || PROVIDER_CONFIG.bKash;
                    return (
                       <div key={p} className="flex items-center justify-between p-4 neu-inset rounded-xl">
                          <span className="font-bold text-sm text-gray-700" style={{ color: cfg.color }}>{p}</span>
                          {health.state === "LIVE" && <span className="badge badge-success text-[10px]">{t("dash.live")}</span>}
                          {health.state === "DELAYED" && <span className="badge badge-warning text-[10px]">{t("dash.delayed")}</span>}
                          {health.state === "MISSING" && <span className="badge badge-critical text-[10px]">{t("dash.missing")}</span>}
                       </div>
                    )
                 })}
              </div>
          </div>

        </div>
      </div>
    </div>
  );
}
