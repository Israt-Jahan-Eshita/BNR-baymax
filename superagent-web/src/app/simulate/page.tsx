"use client";

import { useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation } from "@/context/SimulationContext";
import { ScenarioType } from "@/domain/models";

function IconPlay() {
  return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>;
}

function IconCheck() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>;
}

export default function SimulatorPage() {
  const { t } = useAppContext();
  const { 
    scenarioDefinitions, 
    isLoadingDefinitions, 
    runScenario, 
    isRunningScenario,
    scenarioError,
    lastScenarioRun,
    refreshScenarioDefinitions
  } = useSimulation();

  const [activeScenarioType, setActiveScenarioType] = useState<ScenarioType | null>(null);
  const [logs, setLogs] = useState<string[]>([]);

  const handleRun = async (scenarioType: ScenarioType) => {
    setActiveScenarioType(scenarioType);
    setLogs([]);
    
    await runScenario(scenarioType, (msg) => {
      setLogs((prev) => [...prev, msg]);
    });
  };

  const allowedScenarios = scenarioDefinitions.filter(s => s.scenarioType !== "EID_DEMAND_SPIKE" as any);

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="flex justify-between items-start mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("sim.title")}</h1>
          <p className="text-sm text-gray-500 mt-1">Inject controlled synthetic conditions into the real backend flow.</p>
        </div>
        <button 
          onClick={refreshScenarioDefinitions}
          className="text-xs font-bold text-gray-500 bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded"
        >
          Refresh Definitions
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <h2 className="text-sm font-bold text-gray-800 uppercase tracking-wider mb-4">{t("sim.new")}</h2>
          
          {isLoadingDefinitions ? (
            <div className="neu-inset p-8 text-center text-gray-500 font-bold rounded-xl">
              Loading scenario definitions from backend...
            </div>
          ) : allowedScenarios.length === 0 ? (
            <div className="neu-inset p-8 text-center text-gray-500 font-bold rounded-xl">
              No scenarios available.
            </div>
          ) : (
            allowedScenarios.map(scenario => (
              <div key={scenario.scenarioType} className="neu-raised p-6 rounded-xl border border-transparent hover:border-gray-200 transition-colors">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="font-bold text-lg text-gray-800 mb-1">{scenario.displayName}</h3>
                    <p className="text-sm text-gray-500">{scenario.description}</p>
                  </div>
                </div>
                
                <div className="flex items-center justify-between mt-6 pt-4 border-t border-gray-100 dark:border-gray-800/50">
                  <div className="text-xs font-bold text-gray-400 max-w-[70%]">
                    Expected System Effect: <span className="text-gray-600 dark:text-gray-300">{scenario.expectedSystemEffect}</span>
                  </div>
                  <button
                    onClick={() => handleRun(scenario.scenarioType)}
                    disabled={isRunningScenario}
                    className={`px-4 py-2 text-sm font-bold rounded-lg flex items-center gap-2 transition-all shrink-0 ${
                      isRunningScenario 
                        ? "bg-gray-100 text-gray-400 cursor-not-allowed" 
                        : "bg-gray-900 text-white hover:bg-black shadow-lg shadow-gray-900/20"
                    }`}
                  >
                    {isRunningScenario && activeScenarioType === scenario.scenarioType ? (
                      <>{t("sim.btnProc")}</>
                    ) : (
                      <><IconPlay /> {t("sim.btnExec")}</>
                    )}
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        <div>
          <h2 className="text-sm font-bold text-gray-800 uppercase tracking-wider mb-4">Run Status</h2>
          <div className="neu-inset p-6 rounded-xl min-h-[400px]">
            {logs.length === 0 ? (
              <div className="text-sm text-gray-500 text-center mt-10">
                Scenario Lab injects controlled synthetic conditions into the real backend flow. It does not inject alerts.
              </div>
            ) : (
              <div className="space-y-4">
                {lastScenarioRun && lastScenarioRun.status === "FAILED" && (
                  <div className="mb-4 p-3 bg-red-50 text-red-700 border border-red-200 rounded text-sm font-bold">
                    FAILED: {scenarioError}
                  </div>
                )}
                {lastScenarioRun && lastScenarioRun.status === "COMPLETED" && (
                  <div className="mb-4 p-3 bg-green-50 text-green-700 border border-green-200 rounded text-sm font-bold">
                    COMPLETED: {lastScenarioRun.committedTransactionCount} transactions committed.
                  </div>
                )}
                {logs.map((log, i) => (
                  <div key={i} className="flex items-start gap-3 animate-in fade-in slide-in-from-bottom-2">
                    <div className="text-sm text-gray-700 font-medium">
                      {log}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
