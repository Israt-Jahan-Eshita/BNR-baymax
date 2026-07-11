"use client";

import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { 
  ScenarioDefinition, 
  ScenarioType, 
  ScenarioRunDetail, 
  DashboardAggregateResponse 
} from "../domain/models";
import { getScenarioDefinitions, runScenario as apiRunScenario } from "../lib/api/scenarios";
import { getDashboard } from "../lib/api/dashboard";

export const DEMO_AGENT_CODE = "AGT-001";

interface SimulationContextType {
  scenarioDefinitions: ScenarioDefinition[];
  isLoadingDefinitions: boolean;
  isRunningScenario: boolean;
  scenarioError: string | null;
  lastScenarioRun: ScenarioRunDetail | null;
  runScenario: (scenarioType: ScenarioType, onProgress: (msg: string) => void) => Promise<void>;
  refreshScenarioDefinitions: () => Promise<void>;
  
  // Signal to other components to re-fetch data
  refreshCounter: number;
  triggerManualRefresh: () => void;
}

const SimulationContext = createContext<SimulationContextType | undefined>(undefined);

export function SimulationProvider({ children }: { children: React.ReactNode }) {
  const [scenarioDefinitions, setScenarioDefinitions] = useState<ScenarioDefinition[]>([]);
  const [isLoadingDefinitions, setIsLoadingDefinitions] = useState(true);
  const [isRunningScenario, setIsRunningScenario] = useState(false);
  const [scenarioError, setScenarioError] = useState<string | null>(null);
  const [lastScenarioRun, setLastScenarioRun] = useState<ScenarioRunDetail | null>(null);
  const [refreshCounter, setRefreshCounter] = useState(0);

  const triggerManualRefresh = useCallback(() => {
    setRefreshCounter(c => c + 1);
  }, []);

  const refreshScenarioDefinitions = useCallback(async () => {
    setIsLoadingDefinitions(true);
    try {
      const defs = await getScenarioDefinitions();
      setScenarioDefinitions(defs);
    } catch (err) {
      console.error("Failed to load scenario definitions", err);
    } finally {
      setIsLoadingDefinitions(false);
    }
  }, []);

  useEffect(() => {
    refreshScenarioDefinitions();
  }, [refreshScenarioDefinitions]);

  const runScenario = async (scenarioType: ScenarioType, onProgress: (msg: string) => void) => {
    setIsRunningScenario(true);
    setScenarioError(null);
    setLastScenarioRun(null);
    onProgress("Injecting synthetic conditions into backend...");

    try {
      const initialSnapshot = await getDashboard(DEMO_AGENT_CODE).catch(() => null);
      
      const result = await apiRunScenario(scenarioType, DEMO_AGENT_CODE);
      setLastScenarioRun(result);

      if (result.status === "FAILED") {
        setScenarioError(result.failureMessage || "Scenario failed.");
        onProgress(`Scenario failed: ${result.failureMessage || "Unknown error"}`);
        setIsRunningScenario(false);
        return;
      }

      onProgress("Scenario completed. Waiting for analytics and policies...");

      // Bounded refresh
      let attempts = 0;
      let stateChanged = false;

      const checkState = async () => {
        try {
          const newState = await getDashboard(DEMO_AGENT_CODE);
          if (initialSnapshot) {
            const oldAlerts = initialSnapshot.recentAlerts.totalElements;
            const newAlerts = newState.recentAlerts.totalElements;
            const oldCases = initialSnapshot.recentCases.totalElements;
            const newCases = newState.recentCases.totalElements;
            const oldCash = initialSnapshot.balances.physicalCashBalance;
            const newCash = newState.balances.physicalCashBalance;

            if (newAlerts > oldAlerts) {
              onProgress("Scenario completed and a new persisted alert was observed.");
              return true;
            }
            if (newCases > oldCases) {
              onProgress("Case creation policy opened a new operational case.");
              return true;
            }
            if (newCash !== oldCash) {
              return true;
            }
          }
        } catch {
          // ignore network errors during polling
        }
        return false;
      };

      const pollInterval = setInterval(async () => {
        attempts++;
        if (attempts >= 12) {
          clearInterval(pollInterval);
          if (!stateChanged) {
            onProgress("Scenario completed. Backend state refreshed.");
          }
          triggerManualRefresh();
          setIsRunningScenario(false);
          return;
        }
        
        stateChanged = await checkState();
        if (stateChanged) {
          clearInterval(pollInterval);
          if (attempts > 0) { // If it wasn't already updated by alerts/cases
            onProgress("Scenario completed. Backend state refreshed.");
          }
          triggerManualRefresh();
          setIsRunningScenario(false);
        }
      }, 750);

    } catch (err: any) {
      setScenarioError(err.message || "Failed to run scenario");
      onProgress(`Error: ${err.message || "Failed to run scenario"}`);
      setIsRunningScenario(false);
    }
  };

  return (
    <SimulationContext.Provider value={{
      scenarioDefinitions,
      isLoadingDefinitions,
      isRunningScenario,
      scenarioError,
      lastScenarioRun,
      runScenario,
      refreshScenarioDefinitions,
      refreshCounter,
      triggerManualRefresh
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
