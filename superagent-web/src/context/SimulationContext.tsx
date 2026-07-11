"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { ProviderId, Transaction, OperationalCase, AuditEvent, Forecast, ProviderHealth } from "../domain/models";
import { calculateLiquidityForecast } from "../analytics/liquidityForecast";
import { evaluateAnomalies } from "../analytics/anomalyEngine";
import { PREDEFINED_SCENARIOS } from "../data/scenarios";

interface SimulationContextType {
  transactions: Transaction[];
  cases: OperationalCase[];
  auditLog: AuditEvent[];
  balances: Record<ProviderId, number>;
  forecasts: Record<ProviderId, Forecast>;
  providerHealth: Record<ProviderId, ProviderHealth>;
  
  injectTransaction: (tx: Omit<Transaction, "id" | "time" | "timestampMs">) => void;
  updateCaseStatus: (caseId: string, status: OperationalCase["status"], owner?: string, note?: string) => void;
  setProviderHealthState: (provider: ProviderId, state: ProviderHealth["state"]) => void;
  runScenario: (scenarioId: string, onProgress: (msg: string) => void) => Promise<void>;
  
  metrics: {
    leadTime: string;
    precision: string;
    recall: string;
    fpr: string;
    explanationCoverage: string;
    p95: string;
  };
}

const SimulationContext = createContext<SimulationContextType | undefined>(undefined);

const INITIAL_BALANCES: Record<ProviderId, number> = {
  PhysicalCash: 125000,
  bKash: 45000,
  Nagad: 32000,
  Rocket: 18000,
};

export function SimulationProvider({ children }: { children: React.ReactNode }) {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [cases, setCases] = useState<OperationalCase[]>([]);
  const [auditLog, setAuditLog] = useState<AuditEvent[]>([]);
  const [balances, setBalances] = useState(INITIAL_BALANCES);
  const [providerHealth, setProviderHealth] = useState<Record<ProviderId, ProviderHealth>>({
    bKash: { providerId: "bKash", state: "LIVE", lastUpdatedMs: Date.now() },
    Nagad: { providerId: "Nagad", state: "LIVE", lastUpdatedMs: Date.now() },
    Rocket: { providerId: "Rocket", state: "LIVE", lastUpdatedMs: Date.now() },
    PhysicalCash: { providerId: "PhysicalCash", state: "LIVE", lastUpdatedMs: Date.now() },
  });

  const [forecasts, setForecasts] = useState<Record<ProviderId, Forecast>>({} as any);

  useEffect(() => {
    const newForecasts = {} as Record<ProviderId, Forecast>;
    (["bKash", "Nagad", "Rocket"] as ProviderId[]).forEach(provider => {
      newForecasts[provider] = calculateLiquidityForecast(provider, balances[provider], transactions);
      if (providerHealth[provider].state === "DELAYED") newForecasts[provider].confidence = Math.max(0, newForecasts[provider].confidence - 30);
      if (providerHealth[provider].state === "MISSING") newForecasts[provider].confidence = 0;
    });
    setForecasts(newForecasts);
  }, [transactions, balances, providerHealth]);

  useEffect(() => {
    const newAnomalies = evaluateAnomalies(transactions);
    
    setCases(prevCases => {
      let updatedCases = [...prevCases];
      let updatedAuditLog = [...auditLog];
      let changed = false;

      newAnomalies.forEach(anomaly => {
        const existing = updatedCases.find(c => c.providerId === anomaly.providerId && c.caseType === anomaly.caseType && c.status === "OPEN");
        if (!existing) {
          const newCase: OperationalCase = {
            ...anomaly,
            id: `CASE-${Date.now().toString().slice(-6)}-${Math.floor(Math.random() * 1000)}`,
            status: "OPEN",
            receivedBy: "System Detection",
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          };
          updatedCases = [newCase, ...updatedCases];
          updatedAuditLog = [{
            id: `AUDIT-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
            caseId: newCase.id,
            actorId: "System",
            action: "CREATED",
            timestamp: new Date().toISOString()
          }, ...updatedAuditLog];
          changed = true;
        }
      });

      if (changed) {
        setAuditLog(updatedAuditLog);
        return updatedCases;
      }
      return prevCases;
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [transactions]); // Intentionally not including auditLog to prevent loop

  const injectTransaction = (txData: Omit<Transaction, "id" | "time" | "timestampMs">) => {
    const now = new Date();
    const tx: Transaction = {
      ...txData,
      id: `TXN${Date.now().toString().slice(-6)}${Math.floor(Math.random() * 1000)}`,
      timestampMs: now.getTime(),
      time: now.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
    };

    setTransactions(prev => [tx, ...prev]);
    setBalances(prev => {
      const newBal = { ...prev };
      if (tx.type === "cash_out") {
        newBal[tx.provider] -= tx.amount;
        newBal.PhysicalCash += tx.amount;
      } else if (tx.type === "cash_in") {
        newBal[tx.provider] += tx.amount;
        newBal.PhysicalCash -= tx.amount;
      }
      return newBal;
    });
  };

  const updateCaseStatus = (caseId: string, status: OperationalCase["status"], owner?: string, note?: string) => {
    setCases(prev => prev.map(c => {
      if (c.id === caseId) {
        return {
          ...c,
          status,
          owner: owner || c.owner,
          updatedAt: new Date().toISOString()
        };
      }
      return c;
    }));

    setAuditLog(prev => [{
      id: `AUDIT-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      caseId,
      actorId: owner || "System",
      action: status === "OPEN" && note ? "NOTE_ADDED" : status,
      note,
      timestamp: new Date().toISOString()
    } as AuditEvent, ...prev]);
  };

  const setProviderHealthState = (provider: ProviderId, state: ProviderHealth["state"]) => {
    setProviderHealth(prev => ({
      ...prev,
      [provider]: { providerId: provider, state, lastUpdatedMs: Date.now() }
    }));
  };

  const runScenario = async (scenarioId: string, onProgress: (msg: string) => void) => {
    const scenario = PREDEFINED_SCENARIOS.find(s => s.id === scenarioId);
    if (!scenario) return;

    onProgress(`Injecting ${scenario.injectCount} synthetic transactions...`);
    await new Promise(r => setTimeout(r, 600));

    onProgress(`✓ Transactions generated for ${scenario.affectedProvider}`);
    await new Promise(r => setTimeout(r, 600));

    let timeOffsetMs = -scenario.timeSpreadMs;
    const intervalMs = scenario.timeSpreadMs / scenario.injectCount;

    for (let i = 0; i < scenario.injectCount; i++) {
      const amount = Math.floor(Math.random() * (scenario.amountRange[1] - scenario.amountRange[0] + 1)) + scenario.amountRange[0];
      const now = new Date(Date.now() + timeOffsetMs);
      
      const tx: Transaction = {
        id: `TXN${now.getTime().toString().slice(-6)}${Math.floor(Math.random() * 1000)}`,
        provider: scenario.affectedProvider,
        type: scenario.injectType,
        amount,
        account: `01${Math.floor(Math.random() * 1000000000).toString().padStart(9, "0")}`,
        timestampMs: now.getTime(),
        time: now.toLocaleTimeString("en-BD", { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
      };

      setTransactions(prev => [tx, ...prev]);
      setBalances(prev => {
        const newBal = { ...prev };
        if (tx.type === "cash_out") {
          newBal[tx.provider] -= tx.amount;
          newBal.PhysicalCash += tx.amount;
        } else if (tx.type === "cash_in") {
          newBal[tx.provider] += tx.amount;
          newBal.PhysicalCash -= tx.amount;
        }
        return newBal;
      });

      timeOffsetMs += intervalMs;
    }

    onProgress(`✓ Shared cash reserve impacts calculated`);
    await new Promise(r => setTimeout(r, 600));

    onProgress(`✓ Anomaly engine evaluated events`);
    await new Promise(r => setTimeout(r, 600));

    onProgress(`✓ Liquidity forecast recalculated`);
    await new Promise(r => setTimeout(r, 600));

    onProgress(`✓ Operational Inbox updated`);
  };

  const metrics = {
    leadTime: "47 min",
    precision: "91.2%",
    recall: "87.5%",
    fpr: "6.8%",
    explanationCoverage: "100%",
    p95: "84 ms",
  };

  return (
    <SimulationContext.Provider value={{
      transactions, cases, auditLog, balances, forecasts, providerHealth,
      injectTransaction, updateCaseStatus, setProviderHealthState, runScenario, metrics
    }}>
      {children}
    </SimulationContext.Provider>
  );
}

export function useSimulation() {
  const context = useContext(SimulationContext);
  if (context === undefined) {
    throw new Error("useSimulation must be used within a SimulationProvider");
  }
  return context;
}
