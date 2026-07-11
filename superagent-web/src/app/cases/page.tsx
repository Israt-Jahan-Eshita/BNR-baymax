"use client";

import { useEffect, useState } from "react";
import { useSimulation, DEMO_AGENT_CODE } from "@/context/SimulationContext";
import { getCases, getCaseDetail } from "@/lib/api/cases";
import { OperationalCaseSummary, OperationalCaseDetail } from "@/domain/models";

function IconShield() {
  return <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>;
}

export default function CasesPage() {
  const { refreshCounter } = useSimulation();
  
  const [cases, setCases] = useState<OperationalCaseSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [selectedCaseCode, setSelectedCaseCode] = useState<string | null>(null);
  const [caseDetail, setCaseDetail] = useState<OperationalCaseDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const fetchCases = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await getCases(DEMO_AGENT_CODE);
      setCases(res.cases);
    } catch (err: any) {
      setError(err.message || "Failed to load cases");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCases();
  }, [refreshCounter]);

  const handleSelectCase = async (caseCode: string) => {
    if (selectedCaseCode === caseCode) return;
    setSelectedCaseCode(caseCode);
    setCaseDetail(null);
    setDetailLoading(true);
    try {
      const detail = await getCaseDetail(caseCode);
      setCaseDetail(detail);
    } catch (err: any) {
      console.error(err);
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="flex justify-between items-start mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Operational Cases</h1>
          <p className="text-sm text-gray-500 mt-1">Review operational cases opened by the backend Case Creation Policy.</p>
        </div>
        <button 
          onClick={fetchCases}
          className="text-xs font-bold text-gray-500 bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded"
        >
          Refresh
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* CASES LIST */}
        <div className="lg:col-span-1 space-y-4">
          {loading ? (
            <div className="p-8 text-center text-gray-500 neu-inset rounded-xl font-bold">Loading operational state...</div>
          ) : error ? (
            <div className="p-8 text-center text-red-500 neu-inset rounded-xl font-bold">{error}</div>
          ) : cases.length === 0 ? (
            <div className="p-8 text-center text-gray-500 neu-inset rounded-xl font-bold">No operational cases are currently available.</div>
          ) : (
            cases.map(c => (
              <div 
                key={c.caseCode} 
                onClick={() => handleSelectCase(c.caseCode)}
                className={`p-4 rounded-xl cursor-pointer transition-colors ${selectedCaseCode === c.caseCode ? "neu-inset border border-gray-300" : "neu-raised border border-transparent hover:border-gray-200"}`}
              >
                <div className="flex items-center justify-between mb-2">
                  <span className={`badge ${c.priority === "CRITICAL" ? "badge-critical" : c.priority === "HIGH" ? "badge-warning" : "badge-info"}`}>
                    {c.priority}
                  </span>
                  <span className="text-xs font-mono text-gray-500 font-bold">{c.caseCode}</span>
                </div>
                <div className="font-bold text-gray-800 text-sm mb-1">{c.title}</div>
                <div className="flex justify-between items-end mt-2">
                  <span className={`badge text-[9px] ${c.status === "OPEN" ? "badge-info" : "badge-success"}`}>
                    {c.status}
                  </span>
                  <div className="text-[10px] text-gray-500 font-bold uppercase">{c.creationSource}</div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* CASE DETAIL */}
        <div className="lg:col-span-2">
          {!selectedCaseCode ? (
            <div className="p-12 text-center text-gray-500 neu-inset rounded-xl font-bold h-full flex items-center justify-center">
              Select a case to view operational details.
            </div>
          ) : detailLoading ? (
            <div className="p-12 text-center text-gray-500 neu-inset rounded-xl font-bold h-full flex items-center justify-center">
              Loading case detail...
            </div>
          ) : caseDetail ? (
            <div className="neu-raised rounded-xl overflow-hidden animate-in">
              {/* Header */}
              <div className="p-6 border-b border-gray-200 dark:border-gray-700/50 flex flex-wrap gap-4 justify-between items-start">
                <div className="flex items-start gap-4">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center neu-inset ${caseDetail.priority === "CRITICAL" || caseDetail.priority === "HIGH" ? "text-red-500" : "text-orange-500"}`}>
                    <IconShield />
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className={`badge ${caseDetail.priority === "CRITICAL" ? "badge-critical" : "badge-warning"}`}>{caseDetail.priority}</span>
                      <span className={`badge ${caseDetail.status === "OPEN" ? "badge-info" : "badge-success"}`}>{caseDetail.status}</span>
                      <span className="text-xs font-mono text-gray-500">#{caseDetail.caseCode}</span>
                    </div>
                    <h3 className="font-bold text-gray-800 text-xl">{caseDetail.title}</h3>
                  </div>
                </div>
                <div className="text-right">
                   <div className="text-xs text-gray-500 font-bold mb-1 uppercase text-right">
                     {caseDetail.creationSource === "AUTO_ALERT_POLICY" ? "Case opened by policy" : 
                      caseDetail.creationSource === "MANUAL_OPERATOR" && !caseDetail.sourceAlertCode ? "Manual operational fallback" : 
                      caseDetail.creationSource}
                   </div>
                   {caseDetail.sourceAlertCode && (
                     <div className="text-xs text-gray-500 font-bold">SOURCE ALERT: <span className="text-gray-800 ml-1">#{caseDetail.sourceAlertCode}</span></div>
                   )}
                </div>
              </div>

              <div className="p-6 grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="space-y-6">
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">DESCRIPTION</h4>
                    <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">{caseDetail.description}</p>
                  </div>
                  
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">RECOMMENDED NEXT STEP</h4>
                    <p className="text-sm text-gray-700 leading-relaxed bg-blue-50 p-3 rounded border border-blue-100">{caseDetail.recommendedNextStep}</p>
                  </div>
                </div>

                <div className="space-y-6">
                  <div>
                    <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">AUDIT TRAIL</h4>
                    <div className="space-y-4">
                      {caseDetail.auditTrail.map((log, i) => (
                        <div key={i} className="flex gap-3 text-sm">
                          <div className="text-xs text-gray-400 font-mono pt-0.5 whitespace-nowrap">
                            {new Date(log.occurredAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                          </div>
                          <div>
                            <span className="font-bold text-gray-700">{log.action}</span>
                            <span className="text-gray-500 ml-2">by {log.actorReference} ({log.actorType})</span>
                            {log.details && <div className="text-[11px] text-gray-600 font-medium mt-1 bg-gray-50 p-2 rounded border border-gray-100">{log.details}</div>}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
