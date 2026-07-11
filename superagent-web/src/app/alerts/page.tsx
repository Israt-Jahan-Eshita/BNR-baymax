"use client";

import { useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation } from "@/context/SimulationContext";
import { OperationalCase, ProviderId } from "@/domain/models";

function IconCheck() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>;
}

function IconArrowRight() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>;
}

function IconAlertTriangle() {
  return <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>;
}

export default function AlertsPage() {
  const { t } = useAppContext();
  const { cases, auditLog, updateCaseStatus } = useSimulation();
  
  const [activeTab, setActiveTab] = useState<"OPEN" | "ESCALATED" | "RESOLVED">("OPEN");

  const filteredCases = cases.filter(c => {
    if (activeTab === "OPEN") return c.status === "OPEN" || c.status === "ACKNOWLEDGED" || c.status === "ASSIGNED";
    return c.status === activeTab;
  });

  const getProviderBadgeColor = (provider: ProviderId) => {
    switch (provider) {
      case "bKash": return "var(--bkash)";
      case "Nagad": return "var(--nagad)";
      case "Rocket": return "var(--rocket)";
      default: return "#475569";
    }
  };

  const handleAction = (caseId: string, action: string) => {
    if (action === "ACKNOWLEDGE") updateCaseStatus(caseId, "ACKNOWLEDGED", "OPS-04");
    if (action === "ASSIGN") updateCaseStatus(caseId, "ASSIGNED", "FO-017");
    if (action === "ESCALATE") updateCaseStatus(caseId, "ESCALATED", "Area Manager");
    if (action === "RESOLVE") updateCaseStatus(caseId, "RESOLVED", "OPS-04");
  };

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("alerts.title")}</h1>
        <p className="text-sm text-gray-500 mt-1">{t("alerts.subtitle")}</p>
      </div>

      <div className="flex gap-2 mb-8 p-1 rounded-xl neu-inset w-fit">
        {["OPEN", "ESCALATED", "RESOLVED"].map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab as any)}
            className={`px-6 py-2.5 rounded-lg text-sm font-bold transition-all ${activeTab === tab ? "neu-raised text-gray-800" : "text-gray-500 hover:text-gray-700"}`}
          >
            {tab === "OPEN" ? t("alerts.tabActive") : tab === "ESCALATED" ? t("alerts.tabEscalated") : t("alerts.tabResolved")}
          </button>
        ))}
      </div>

      <div className="space-y-6">
        {filteredCases.length === 0 ? (
          <div className="p-12 text-center text-gray-500 neu-inset rounded-xl">
            No {activeTab} cases found.
          </div>
        ) : (
          filteredCases.map(c => (
            <div key={c.id} className="neu-raised rounded-xl overflow-hidden animate-in">
              {/* Header */}
              <div className="p-5 border-b border-gray-200 dark:border-gray-700/50 flex flex-wrap gap-4 justify-between items-start">
                <div className="flex items-start gap-4">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center neu-inset ${c.severity === "CRITICAL" || c.severity === "HIGH" ? "text-red-500" : "text-orange-500"}`}>
                    <IconAlertTriangle />
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="badge" style={{ background: getProviderBadgeColor(c.providerId), color: "white" }}>{c.providerId}</span>
                      <span className={`badge ${c.severity === "CRITICAL" ? "badge-critical" : "badge-warning"}`}>{c.caseType}</span>
                      <span className="text-xs font-mono text-gray-500">#{c.id}</span>
                    </div>
                    <h3 className="font-bold text-gray-800 text-lg">{c.title}</h3>
                  </div>
                </div>
                <div className="text-right">
                   <div className="text-xs text-gray-500 font-bold mb-1">{t("alerts.confidence")} <span className="text-gray-800">{c.confidence}%</span></div>
                   <div className="text-xs text-gray-500 font-bold">{t("case.owner")}: <span className="text-gray-800">{c.owner || "Unassigned"}</span></div>
                </div>
              </div>

              <div className="p-6 grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="space-y-6">
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">{t("alerts.whyFlagged")}</h4>
                    <ul className="space-y-2 text-sm text-gray-700 font-medium list-disc list-inside">
                      {c.whyFlagged.map((f, i) => <li key={i}>{f}</li>)}
                    </ul>
                  </div>

                  {c.possibleNormalExplanation && (
                     <div>
                        <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">{t("alerts.possibleNormal")}</h4>
                        <div className="text-sm text-gray-700 bg-green-50/50 dark:bg-green-900/10 p-3 rounded-lg border border-green-100 dark:border-green-900/30">
                           {c.possibleNormalExplanation}
                        </div>
                     </div>
                  )}

                  {c.uncertaintyNotice && (
                     <div>
                        <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">{t("alerts.uncertainty")}</h4>
                        <div className="text-sm text-gray-700 bg-orange-50/50 dark:bg-orange-900/10 p-3 rounded-lg border border-orange-100 dark:border-orange-900/30">
                           {c.uncertaintyNotice}
                        </div>
                     </div>
                  )}
                  
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">{t("alerts.safeNextStep")}</h4>
                    <p className="text-sm text-gray-700">{c.recommendedNextStep}</p>
                  </div>
                </div>

                <div className="space-y-6">
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">{t("case.timeline")} / {t("case.auditHistory")}</h4>
                    <div className="space-y-4">
                      {auditLog.filter(log => log.caseId === c.id).reverse().map((log, i) => (
                        <div key={log.id} className="flex gap-3 text-sm">
                          <div className="text-xs text-gray-400 font-mono pt-0.5 whitespace-nowrap">
                            {new Date(log.timestamp).toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit" })}
                          </div>
                          <div>
                            <span className="font-bold text-gray-700">{log.action.replace("_", " ")}</span>
                            <span className="text-gray-500 ml-2">by {log.actorId}</span>
                            {log.note && <div className="text-gray-600 italic mt-1 bg-gray-50 dark:bg-gray-800/50 p-2 rounded">"{log.note}"</div>}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {/* Actions Footer */}
              {c.status !== "RESOLVED" && (
                <div className="p-4 bg-gray-50/50 dark:bg-gray-800/20 border-t border-gray-200 dark:border-gray-700/50 flex gap-3 flex-wrap">
                  {c.status === "OPEN" && (
                     <button onClick={() => handleAction(c.id, "ACKNOWLEDGE")} className="px-4 py-2 text-sm font-bold bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors">
                        {t("alerts.actionAcknowledge")}
                     </button>
                  )}
                  {(c.status === "OPEN" || c.status === "ACKNOWLEDGED") && (
                     <button onClick={() => handleAction(c.id, "ASSIGN")} className="px-4 py-2 text-sm font-bold bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 text-gray-700 transition-colors">
                        {t("case.assign")}
                     </button>
                  )}
                  {c.status !== "ESCALATED" && (
                     <button onClick={() => handleAction(c.id, "ESCALATE")} className="px-4 py-2 text-sm font-bold text-orange-600 bg-orange-50 dark:bg-orange-900/20 rounded-lg hover:bg-orange-100 transition-colors">
                        {t("alerts.actionEscalate")}
                     </button>
                  )}
                  <button onClick={() => handleAction(c.id, "RESOLVE")} className="px-4 py-2 text-sm font-bold text-white bg-gray-900 dark:bg-gray-700 rounded-lg hover:bg-black transition-colors ml-auto flex items-center gap-2">
                     <IconCheck /> {t("alerts.actionResolve")}
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
