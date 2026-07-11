"use client";

import { useEffect, useState } from "react";

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
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const interval = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const fetchData = () => {
      fetch("http://localhost:8080/api/dashboard/1")
        .then((res) => res.json())
        .then((d) => { setData(d); setLoading(false); })
        .catch(() => {
          setData({
            physicalCash: 145200,
            totalEMoney: 245250,
            totalLiquidity: 390450,
            transactionsToday: 847,
            providers: [
              { name: "bKash", balance: 182400, capacity: 78, trend: -12, txCount: 423 },
              { name: "Nagad", balance: 34100, capacity: 32, trend: -28, txCount: 289 },
              { name: "Rocket", balance: 28750, capacity: 54, trend: 5, txCount: 135 },
            ],
            recentTransactions: [
              { id: 1, provider: "bKash", type: "cash_out", amount: 25000, account: "01712****78", time: "12:34:22", status: "completed" },
              { id: 2, provider: "bKash", type: "cash_out", amount: 24500, account: "01712****33", time: "12:33:18", status: "completed" },
              { id: 3, provider: "Nagad", type: "cash_in", amount: 12500, account: "01812****45", time: "12:31:05", status: "completed" },
              { id: 4, provider: "Rocket", type: "cash_out", amount: 8000, account: "01512****92", time: "12:28:41", status: "completed" },
              { id: 5, provider: "bKash", type: "cash_out", amount: 25000, account: "01712****78", time: "12:26:10", status: "completed" },
              { id: 6, provider: "bKash", type: "cash_out", amount: 24800, account: "01612****19", time: "12:24:33", status: "completed" },
              { id: 7, provider: "Nagad", type: "cash_in", amount: 5000, account: "01912****67", time: "12:22:15", status: "completed" },
              { id: 8, provider: "Rocket", type: "cash_out", amount: 3500, account: "01512****44", time: "12:20:02", status: "completed" },
            ],
            activeAlerts: [
              {
                id: 1,
                severity: "critical",
                provider: "bKash",
                title: "Liquidity Depletion Warning",
                message: "বর্তমান লেনদেনের ধারা অনুযায়ী বিকেল ৫টা ২০ মিনিটের মধ্যে আপনার নগদ টাকা শেষ হয়ে যেতে পারে। সবচেয়ে বেশি চাপ আসছে বিকাশ ক্যাশ-আউট থেকে।",
                timeToDepletion: "2h 46m",
                requiredCash: 120000,
              },
            ],
            liquidityForecast: [
              { time: "Now", cash: 145200, bkash: 182400, nagad: 34100, rocket: 28750 },
              { time: "+1h", cash: 98000, bkash: 142000, nagad: 22000, rocket: 25000 },
              { time: "+2h", cash: 52000, bkash: 95000, nagad: 12000, rocket: 21000 },
              { time: "+3h", cash: 8000, bkash: 48000, nagad: 3000, rocket: 18000 },
              { time: "+4h", cash: 0, bkash: 12000, nagad: 0, rocket: 15000 },
            ],
          });
          setLoading(false);
        });
    };
    fetchData();
    const polling = setInterval(fetchData, 10000);
    return () => clearInterval(polling);
  }, []);

  if (loading || !data) {
    return (
      <div className="p-8 flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="w-8 h-8 border-2 border-gray-400 border-t-gray-800 rounded-full animate-spin mx-auto mb-4" />
          <p className="text-sm text-gray-600 font-medium">Loading dashboard data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 lg:p-8 max-w-[1600px] mx-auto">
      {/* === TOP BAR === */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Agent Dashboard</h1>
          <p className="text-sm text-gray-500 mt-1">Outlet #A-1042 &middot; Dhaka North &middot; Mirpur-10</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 text-xs text-gray-600 px-4 py-2 rounded-lg neu-inset font-bold">
            <div className="live-dot" />
            <span>Live</span>
            <span className="text-gray-400">|</span>
            <span>{currentTime.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}</span>
          </div>
          <div className="text-xs text-gray-600 px-4 py-2 rounded-lg neu-inset font-bold">
            Agent ID: AGT-1042
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-8">
        {/* === MAIN CONTENT (Left 3 cols) === */}
        <div className="xl:col-span-3 space-y-8">
          
          {/* CRITICAL ALERT BANNER */}
          {data.activeAlerts?.length > 0 && (
            <div className="neu-raised border-l-4 overflow-hidden animate-in" style={{ borderLeftColor: "var(--danger)" }}>
              <div className="p-5 flex gap-4">
                <div className="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--danger)" }}>
                  <IconAlertTriangle />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 mb-2">
                    <span className="badge badge-critical">Critical</span>
                    <span className="badge badge-bkash">{data.activeAlerts[0].provider}</span>
                    <span className="text-xs text-gray-500 font-bold flex items-center gap-1"><IconClock /> Estimated depletion: {data.activeAlerts[0].timeToDepletion}</span>
                  </div>
                  <p className="text-[15px] text-gray-800 font-medium leading-relaxed mb-3">{data.activeAlerts[0].message}</p>
                  <div className="flex items-center gap-4 text-xs">
                    <span className="font-bold text-gray-600">Required additional cash: ৳{data.activeAlerts[0].requiredCash?.toLocaleString("en-BD")}</span>
                    <a href="/alerts" className="font-bold flex items-center gap-1 hover:underline" style={{ color: "var(--bkash)" }}>
                      View in Operations Inbox <IconArrowRight />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* SUMMARY METRICS */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="neu-raised p-6">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">Total Liquidity</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--info)" }}><IconActivity /></div>
              </div>
              <div className="metric-value">৳{data.totalLiquidity?.toLocaleString("en-BD")}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">Cash + All provider e-money combined</p>
            </div>

            <div className="neu-raised p-6">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">Physical Cash</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--cash)" }}><IconCash /></div>
              </div>
              <div className="metric-value" style={{ color: "var(--cash)" }}>৳{data.physicalCash?.toLocaleString("en-BD")}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">Shared across all providers</p>
            </div>

            <div className="neu-raised p-6">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">Total E-Money</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--bkash)" }}><IconCash /></div>
              </div>
              <div className="metric-value">৳{data.totalEMoney?.toLocaleString("en-BD")}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">Sum of 3 provider balances</p>
            </div>

            <div className="neu-raised p-6">
              <div className="flex items-center justify-between mb-4">
                <span className="metric-label">Transactions Today</span>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center neu-inset" style={{ color: "var(--warning)" }}><IconUsers /></div>
              </div>
              <div className="metric-value">{data.transactionsToday}</div>
              <p className="text-xs text-gray-500 mt-2 font-medium">Across all providers</p>
            </div>
          </div>

          {/* PROVIDER BALANCES */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {data.providers.map((p: any) => {
              const cfg = PROVIDER_CONFIG[p.name] || PROVIDER_CONFIG.bKash;
              const isLow = p.capacity < 40;
              return (
                <div key={p.name} className="neu-raised card-interactive p-6 border-l-4" style={{ borderLeftColor: cfg.color }}>
                  <div className="flex items-center justify-between mb-5">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm text-white" style={{ background: cfg.color, boxShadow: `4px 4px 8px rgba(0,0,0,0.1)` }}>
                        {p.name[0]}
                      </div>
                      <div>
                        <div className="font-bold text-gray-800">{p.name}</div>
                        <div className="text-xs text-gray-500 font-medium">{p.txCount} transactions today</div>
                      </div>
                    </div>
                    <div className={`flex items-center gap-1 text-xs font-bold px-2 py-1 rounded neu-inset ${p.trend < 0 ? "text-red-500" : "text-green-600"}`}>
                      {p.trend < 0 ? <IconTrendDown /> : <IconTrendUp />}
                      {Math.abs(p.trend)}% / hr
                    </div>
                  </div>

                  <div className="text-2xl font-bold text-gray-800 mb-5 tracking-tight">
                    ৳{p.balance.toLocaleString("en-BD")}
                  </div>

                  <div>
                    <div className="flex items-center justify-between text-xs mb-2">
                      <span className="text-gray-500 font-bold">Capacity</span>
                      <span className={`font-bold ${isLow ? "text-red-500" : "text-gray-700"}`}>{p.capacity}%</span>
                    </div>
                    <div className="progress-track">
                      <div
                        className="progress-fill"
                        style={{
                          width: `${p.capacity}%`,
                          background: isLow ? "var(--danger)" : cfg.color,
                        }}
                      />
                    </div>
                    {isLow && (
                      <p className="text-[11px] text-red-500 font-bold mt-2 text-right">Review required</p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* LIQUIDITY FORECAST */}
          {data.liquidityForecast && (
            <div className="neu-raised p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="font-bold text-gray-800 text-lg">Liquidity Forecast</h3>
                  <p className="text-xs text-gray-500 mt-1 font-medium">Projected balances based on current transaction velocity</p>
                </div>
                <span className="badge badge-warning">Predictive</span>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="table-header border-b border-gray-300">
                      <th className="text-left py-3 px-4 text-gray-500 font-bold">Time</th>
                      <th className="text-right py-3 px-4 text-gray-500 font-bold">Physical Cash</th>
                      <th className="text-right py-3 px-4 text-gray-500 font-bold">bKash</th>
                      <th className="text-right py-3 px-4 text-gray-500 font-bold">Nagad</th>
                      <th className="text-right py-3 px-4 text-gray-500 font-bold">Rocket</th>
                      <th className="text-right py-3 px-4 text-gray-500 font-bold">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.liquidityForecast.map((row: any, i: number) => {
                      const anyZero = row.cash === 0 || row.nagad === 0;
                      return (
                        <tr key={i} className={`table-row ${anyZero ? "bg-red-50/20" : ""}`}>
                          <td className="py-4 px-4 font-bold text-gray-800">{row.time}</td>
                          <td className={`py-4 px-4 text-right font-mono ${row.cash === 0 ? "text-red-600 font-bold" : "text-gray-700 font-medium"}`}>
                            ৳{row.cash.toLocaleString("en-BD")}
                          </td>
                          <td className={`py-4 px-4 text-right font-mono ${row.bkash < 20000 ? "text-red-600 font-bold" : "text-gray-700 font-medium"}`}>
                            ৳{row.bkash.toLocaleString("en-BD")}
                          </td>
                          <td className={`py-4 px-4 text-right font-mono ${row.nagad === 0 ? "text-red-600 font-bold" : "text-gray-700 font-medium"}`}>
                            ৳{row.nagad.toLocaleString("en-BD")}
                          </td>
                          <td className="py-4 px-4 text-right font-mono text-gray-700 font-medium">
                            ৳{row.rocket.toLocaleString("en-BD")}
                          </td>
                          <td className="py-4 px-4 text-right">
                            {anyZero ? (
                              <span className="badge badge-critical">Depleted</span>
                            ) : row.cash < 50000 || row.nagad < 10000 ? (
                              <span className="badge badge-warning">At Risk</span>
                            ) : (
                              <span className="badge badge-success">Healthy</span>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>

        {/* === RIGHT SIDE PANEL (col-span-1) === */}
        <div className="xl:col-span-1">
          <div className="sticky top-6 neu-inset p-5 rounded-2xl border border-white/10">
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="font-bold text-gray-800 text-lg">Recent Tx</h3>
                <p className="text-xs text-gray-500 mt-1 font-medium">Live Feed</p>
              </div>
              <button className="btn btn-sm btn-ghost">View All</button>
            </div>

            <div className="space-y-4">
              {data.recentTransactions.map((tx: any) => {
                const cfg = PROVIDER_CONFIG[tx.provider] || PROVIDER_CONFIG.bKash;
                return (
                  <div key={tx.id} className="neu-raised p-4">
                    <div className="flex justify-between items-center mb-2">
                      <span className={`badge badge-${tx.provider.toLowerCase()}`}>{tx.provider}</span>
                      <span className="text-[10px] font-mono text-gray-500 font-bold">{tx.time}</span>
                    </div>
                    <div className="flex justify-between items-end">
                      <div>
                        <div className="text-sm font-bold text-gray-700 capitalize">{tx.type.replace("_", " ")}</div>
                        <div className="text-xs font-mono text-gray-500 mt-0.5">{tx.account}</div>
                      </div>
                      <div className={`text-right font-bold font-mono ${tx.type === "cash_out" ? "text-red-500" : "text-green-600"}`}>
                        {tx.type === "cash_out" ? "-" : "+"}৳{tx.amount.toLocaleString("en-BD")}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
