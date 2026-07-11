"use client";

import { useState } from "react";
import { useAppContext } from "@/context/AppContext";
import { useSimulation } from "@/context/SimulationContext";
import { PREDEFINED_SCENARIOS } from "@/data/scenarios";

function IconPlay() {
  return <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>;
}

function IconCheck() {
  return <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>;
}

export default function SimulatorPage() {
  const { t } = useAppContext();
  const { runScenario } = useSimulation();

  const [activeScenarioId, setActiveScenarioId] = useState<string | null>(null);
  const [running, setRunning] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  const handleRun = async (scenarioId: string) => {
    setActiveScenarioId(scenarioId);
    setRunning(true);
    setLogs([]);
    
    await runScenario(scenarioId, (msg) => {
      setLogs((prev) => [...prev, msg]);
    });
    
    setRunning(false);
  };

  return (
    <div className="p-6 lg:p-8 max-w-[1200px] mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-800 tracking-tight">{t("sim.title")}</h1>
        <p className="text-sm text-gray-500 mt-1">{t("sim.subtitle")}</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <h2 className="text-sm font-bold text-gray-800 uppercase tracking-wider mb-4">{t("sim.new")}</h2>
          
          {PREDEFINED_SCENARIOS.map(scenario => (
            <div key={scenario.id} className="neu-raised p-6 rounded-xl border border-transparent hover:border-gray-200 transition-colors">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="font-bold text-lg text-gray-800 mb-1">{scenario.title}</h3>
                  <p className="text-sm text-gray-500">{scenario.description}</p>
                </div>
                <span className="badge badge-info">{scenario.affectedProvider}</span>
              </div>
              
              <div className="flex items-center justify-between mt-6 pt-4 border-t border-gray-100 dark:border-gray-800/50">
                <div className="text-xs font-bold text-gray-400">
                  Expected: <span className="text-gray-600 dark:text-gray-300">{scenario.expectedSignal}</span>
                </div>
                <button
                  onClick={() => handleRun(scenario.id)}
                  disabled={running}
                  className={`px-4 py-2 text-sm font-bold rounded-lg flex items-center gap-2 transition-all ${
                    running && activeScenarioId === scenario.id 
                      ? "bg-gray-100 text-gray-400 cursor-not-allowed" 
                      : "bg-gray-900 text-white hover:bg-black shadow-lg shadow-gray-900/20"
                  }`}
                >
                  {running && activeScenarioId === scenario.id ? (
                    <>{t("sim.btnProc")}</>
                  ) : (
                    <><IconPlay /> {t("sim.btnExec")}</>
                  )}
                </button>
              </div>
            </div>
          ))}
        </div>

        <div>
          <h2 className="text-sm font-bold text-gray-800 uppercase tracking-wider mb-4">{t("sim.history")}</h2>
          <div className="neu-inset p-6 rounded-xl min-h-[400px]">
            {logs.length === 0 ? (
              <div className="text-sm text-gray-500 text-center mt-10">
                {t("sim.historyEmpty")}
              </div>
            ) : (
              <div className="space-y-4">
                {logs.map((log, i) => (
                  <div key={i} className="flex items-start gap-3 animate-in fade-in slide-in-from-bottom-2">
                    <div className={`mt-0.5 shrink-0 ${log.startsWith("✓") ? "text-green-500" : "text-gray-400"}`}>
                      {log.startsWith("✓") ? <IconCheck /> : <div className="w-2 h-2 rounded-full bg-blue-500 mt-1.5" />}
                    </div>
                    <div className={`text-sm ${log.startsWith("✓") ? "text-gray-800 font-medium" : "text-gray-500 font-mono"}`}>
                      {log.replace("✓", "").trim()}
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
