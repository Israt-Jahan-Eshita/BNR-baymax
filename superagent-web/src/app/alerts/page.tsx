"use client";

import { useEffect, useState } from "react";

/* ===== SVG Icons ===== */
function IconAlertTriangle() {
  return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>;
}
function IconCheck() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>;
}
function IconArrowUp() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg>;
}
function IconUser() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>;
}
function IconFilter() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>;
}
function IconRefresh() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>;
}
function IconChevronRight() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 18 15 12 9 6"/></svg>;
}
function IconFileText() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>;
}

const PROVIDER_COLORS: Record<string, string> = {
  bKash: "#e2136e",
  Nagad: "#f26522",
  Rocket: "#7c3aed",
};

const STATUS_MAP: Record<string, { label: string; cls: string }> = {
  OPEN: { label: "Open", cls: "badge-critical" },
  ACKNOWLEDGED: { label: "Acknowledged", cls: "badge-info" },
  ESCALATED: { label: "Escalated", cls: "badge-warning" },
  RESOLVED: { label: "Resolved", cls: "badge-success" },
};

const SEVERITY_MAP: Record<string, { cls: string }> = {
  critical: { cls: "badge-critical" },
  warning: { cls: "badge-warning" },
  info: { cls: "badge-info" },
};

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/alerts")
      .then((res) => res.json())
      .then((d) => { setAlerts(Array.isArray(d) ? d : []); setLoading(false); })
      .catch(() => {
        setAlerts([
          {
            id: 1,
            severity: "critical",
            provider: "bKash",
            timestamp: "2026-07-11T12:34:00",
            title: "Anomalous Cash-Out Velocity",
            message: "গত ১২ মিনিটে স্বাভাবিকের তুলনায় অনেক বেশি ক্যাশ-আউট হয়েছে। কয়েকটি লেনদেনের পরিমাণ প্রায় একই এবং অল্প কয়েকটি অ্যাকাউন্ট থেকে বারবার অনুরোধ এসেছে। এটি ঈদ-পূর্ব স্বাভাবিক চাহিদাও হতে পারে, তবে বড় অঙ্কের নগদ পুনরায় সরবরাহের আগে লেনদেনগুলো পর্যালোচনা করা প্রয়োজন।",
            status: "OPEN",
            assignedTo: "Field Officer",
            confidence: 82,
            category: "Velocity Spike",
            evidence: [
              "5 cash-out requests exceeding ৳10,000 in the last 12 minutes",
              "3 originating accounts show repeated requests within 4-minute intervals",
              "Historical baseline velocity for this outlet at this hour: < 1 request/hour",
              "Near-identical amounts (৳24,500 - ৳25,000) across 4 of 5 flagged transactions"
            ],
            escalationPath: ["System Detection", "Field Officer", "Area Manager", "Risk & Compliance"],
            currentStep: 1,
            notes: [],
          },
          {
            id: 2,
            severity: "warning",
            provider: "Nagad",
            timestamp: "2026-07-11T12:20:00",
            title: "Provider Balance Depletion",
            message: "নগদ প্রোভাইডারের ব্যালেন্স দ্রুত কমছে। আনুমানিক ৪৫ মিনিটের মধ্যে সেবা ব্যাহত হতে পারে।",
            status: "ACKNOWLEDGED",
            assignedTo: "Agent",
            confidence: 94,
            category: "Liquidity Risk",
            evidence: [
              "Current outflow rate: ৳45,000/hour (2.3x baseline)",
              "Remaining provider capacity: 32% (৳34,100 of ৳106,000)",
              "Projected service disruption in approximately 45 minutes at current rate"
            ],
            escalationPath: ["System Detection", "Agent", "Field Officer", "Area Manager"],
            currentStep: 1,
            notes: [{ author: "Agent", time: "12:22 PM", text: "Aware of the situation. Eid demand expected. Monitoring closely." }],
          },
          {
            id: 3,
            severity: "info",
            provider: "Rocket",
            timestamp: "2026-07-11T11:55:00",
            title: "Demand Pattern — Pre-Holiday",
            message: "রকেট ক্যাশ-ইন বেড়েছে। স্বাভাবিক ঈদ-পূর্ব চাহিদা হতে পারে।",
            status: "RESOLVED",
            assignedTo: "System",
            confidence: 97,
            category: "Normal Pattern",
            evidence: [
              "Cash-in volume increased 35% compared to same period last week",
              "Pattern matches pre-Eid demand from previous years (2024, 2025)",
              "No repeat-account or split-transaction anomalies detected"
            ],
            escalationPath: ["System Detection", "Auto-Classified"],
            currentStep: 1,
            notes: [
              { author: "System", time: "11:55 AM", text: "Auto-classified as normal pre-holiday demand pattern. No action required." },
              { author: "System", time: "12:10 PM", text: "Pattern confirmed. Alert resolved." }
            ],
          },
        ]);
        setLoading(false);
      });
  }, []);

  const handleAction = (id: number, action: string) => {
    const statusMap: Record<string, string> = {
      acknowledge: "ACKNOWLEDGED",
      escalate: "ESCALATED",
      resolve: "RESOLVED",
    };
    const assignMap: Record<string, string> = {
      acknowledge: "Field Officer",
      escalate: "Area Manager",
      resolve: "Field Officer",
    };

    setAlerts((prev) =>
      prev.map((a) =>
        a.id === id
          ? {
              ...a,
              status: statusMap[action] || a.status,
              assignedTo: assignMap[action] || a.assignedTo,
              currentStep: action === "escalate" ? (a.currentStep || 0) + 1 : a.currentStep,
              notes: [
                ...(a.notes || []),
                { author: "You", time: new Date().toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit" }), text: `Alert ${action}d.` },
              ],
            }
          : a
      )
    );

    fetch(`http://localhost:8080/api/alerts/${id}/status`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: statusMap[action], assignedTo: assignMap[action], notes: `Alert ${action}d` }),
    }).catch(() => {});
  };

  const filtered = filter === "ALL" ? alerts : alerts.filter((a) => a.status === filter);
  const counts = {
    ALL: alerts.length,
    OPEN: alerts.filter((a) => a.status === "OPEN").length,
    ACKNOWLEDGED: alerts.filter((a) => a.status === "ACKNOWLEDGED").length,
    ESCALATED: alerts.filter((a) => a.status === "ESCALATED").length,
    RESOLVED: alerts.filter((a) => a.status === "RESOLVED").length,
  };

  if (loading) {
    return (
      <div className="p-8 flex items-center justify-center min-h-screen">
        <div className="w-8 h-8 border-2 border-gray-300 border-t-gray-800 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="p-6 lg:p-8 max-w-[1400px]">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Alerts & Case Management</h1>
          <p className="text-sm text-gray-500 mt-1">AI-generated alerts with human review and coordination workflow</p>
        </div>
        <div className="flex items-center gap-2">
          <button className="btn btn-sm" onClick={() => window.location.reload()}>
            <IconRefresh /> Refresh
          </button>
        </div>
      </div>

      {/* Status Tabs */}
      <div className="flex items-center gap-1 mb-6 neu-inset p-1 rounded-xl w-fit">
        {(["ALL", "OPEN", "ACKNOWLEDGED", "ESCALATED", "RESOLVED"] as const).map((s) => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${
              filter === s ? "neu-raised text-gray-800" : "text-gray-500 hover:text-gray-800"
            }`}
          >
            {s === "ALL" ? "All" : s.charAt(0) + s.slice(1).toLowerCase()} ({counts[s]})
          </button>
        ))}
      </div>

      {/* Alert List */}
      <div className="space-y-4">
        {filtered.map((alert) => {
          const expanded = expandedId === alert.id;
          const provColor = PROVIDER_COLORS[alert.provider] || "#6b7280";
          const severityCfg = SEVERITY_MAP[alert.severity] || SEVERITY_MAP.info;
          const statusCfg = STATUS_MAP[alert.status] || STATUS_MAP.OPEN;

          return (
            <div key={alert.id} className={`neu-raised overflow-hidden border-l-4 ${alert.status === "RESOLVED" ? "opacity-70" : ""}`} style={{ borderLeftColor: provColor }}>
              {/* Summary Row */}
              <div className="p-5 cursor-pointer" onClick={() => setExpandedId(expanded ? null : alert.id)}>
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-9 h-9 rounded-lg flex items-center justify-center text-white font-bold text-xs" style={{ background: provColor }}>
                    {alert.provider[0]}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                      <span className={`badge ${severityCfg.cls}`}>{alert.severity}</span>
                      <span className={`badge badge-${alert.provider.toLowerCase()}`}>{alert.provider}</span>
                      <span className={`badge ${statusCfg.cls}`}>{statusCfg.label}</span>
                      {alert.category && <span className="text-xs text-gray-400 font-medium">{alert.category}</span>}
                    </div>
                    <h3 className="font-semibold text-gray-900 text-[15px]">{alert.title || "Alert"}</h3>
                    <p className="text-sm text-gray-600 mt-1 line-clamp-2">{alert.message}</p>
                  </div>
                  <div className="flex-shrink-0 flex flex-col items-end gap-2">
                    <span className="text-xs text-gray-400">
                      {new Date(alert.timestamp).toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit" })}
                    </span>
                    <div className="flex items-center gap-1 text-xs text-gray-400">
                      <IconUser /> {alert.assignedTo}
                    </div>
                    <div className={`transition-transform ${expanded ? "rotate-90" : ""}`}>
                      <IconChevronRight />
                    </div>
                  </div>
                </div>
              </div>

              {/* Expanded Detail */}
              {expanded && (
                <div className="border-t border-white/20 neu-inset p-5 animate-in">
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Left: AI Analysis */}
                    <div>
                      <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">AI Analysis</h4>
                      <p className="text-sm text-gray-700 leading-relaxed mb-4">{alert.message}</p>

                      {alert.confidence && (
                        <div className="flex items-center gap-3 mb-4 p-4 neu-flat">
                          <svg width="48" height="48" viewBox="0 0 48 48">
                            <circle cx="24" cy="24" r="20" fill="none" stroke="#e5e7eb" strokeWidth="4" />
                            <circle
                              cx="24" cy="24" r="20" fill="none"
                              stroke={alert.confidence >= 90 ? "var(--success)" : alert.confidence >= 70 ? "var(--warning)" : "var(--danger)"}
                              strokeWidth="4" strokeLinecap="round"
                              strokeDasharray={`${(alert.confidence / 100) * 125.6} 125.6`}
                              transform="rotate(-90 24 24)"
                              className="confidence-ring"
                            />
                            <text x="24" y="27" textAnchor="middle" fontSize="11" fontWeight="700" fill="#374151">{alert.confidence}%</text>
                          </svg>
                          <div>
                            <div className="text-xs font-semibold text-gray-900">Detection Confidence</div>
                            <div className="text-[11px] text-gray-500">This is an advisory score. Human review is required before any action.</div>
                          </div>
                        </div>
                      )}

                      {/* Evidence */}
                      {alert.evidence && (
                        <div>
                          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">Supporting Evidence</h4>
                          <ul className="space-y-2">
                            {alert.evidence.map((ev: string, i: number) => (
                              <li key={i} className="flex items-start gap-2 text-sm text-gray-600 neu-flat p-3">
                                <span className="flex-shrink-0 w-5 h-5 rounded-full neu-inset flex items-center justify-center text-[10px] font-bold text-gray-500 mt-0.5">{i + 1}</span>
                                {ev}
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>

                    {/* Right: Coordination & Notes */}
                    <div>
                      {/* Escalation Path */}
                      {alert.escalationPath && (
                        <div className="mb-5">
                          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Escalation Path</h4>
                          <div className="flex items-center flex-wrap gap-1">
                            {alert.escalationPath.map((step: string, i: number) => (
                              <div key={i} className="flex items-center gap-1">
                                <span className={`text-xs px-3 py-1.5 rounded-lg font-bold ${
                                  i <= (alert.currentStep || 0)
                                    ? "neu-raised text-gray-800"
                                    : "neu-inset text-gray-500"
                                }`}>{step}</span>
                                {i < alert.escalationPath.length - 1 && (
                                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" strokeWidth="2"><polyline points="9 18 15 12 9 6"/></svg>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Case Notes */}
                      <div className="mb-5">
                        <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Case Notes</h4>
                        {(alert.notes && alert.notes.length > 0) ? (
                          <div className="space-y-2">
                            {alert.notes.map((note: any, i: number) => (
                              <div key={i} className="flex items-start gap-2 p-3 neu-flat">
                                <div className="w-7 h-7 rounded-full neu-inset flex items-center justify-center flex-shrink-0">
                                  <IconUser />
                                </div>
                                <div>
                                  <div className="text-xs"><span className="font-semibold text-gray-900">{note.author}</span> <span className="text-gray-400">{note.time}</span></div>
                                  <div className="text-sm text-gray-600 mt-0.5">{note.text}</div>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="text-xs text-gray-400 italic">No case notes recorded yet.</p>
                        )}
                      </div>

                      {/* Actions */}
                      {alert.status !== "RESOLVED" && (
                        <div>
                          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Actions</h4>
                          <div className="flex flex-wrap gap-2">
                            {alert.status === "OPEN" && (
                              <button onClick={() => handleAction(alert.id, "acknowledge")} className="btn btn-sm btn-primary">
                                <IconCheck /> Acknowledge
                              </button>
                            )}
                            <button onClick={() => handleAction(alert.id, "escalate")} className="btn btn-sm btn-warning">
                              <IconArrowUp /> Escalate
                            </button>
                            <button onClick={() => handleAction(alert.id, "resolve")} className="btn btn-sm btn-success">
                              <IconCheck /> Resolve
                            </button>
                          </div>
                        </div>
                      )}

                      {alert.status === "RESOLVED" && (
                        <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
                          <div className="flex items-center gap-2 text-sm font-semibold text-green-700">
                            <IconCheck /> Case Resolved
                          </div>
                          <p className="text-xs text-green-600 mt-1">This alert has been reviewed and closed. No further action is required.</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Responsible Design Notice */}
      <div className="mt-8 p-5 neu-flat">
        <div className="flex items-start gap-3">
          <IconFileText />
          <div>
            <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wider">Responsible Design Notice</h4>
            <p className="text-[11px] text-gray-500 mt-1 leading-relaxed">
              All alerts are advisory and generated by AI-based pattern detection. An anomaly flag does not constitute proof of fraud. 
              Human review is mandatory before any escalation decision. Provider data boundaries are maintained — no cross-provider 
              balance merging or unauthorized actions are performed.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
