import { ProviderId } from "../domain/models";

export interface ScenarioDefinition {
  id: string;
  title: string;
  description: string;
  affectedProvider: ProviderId;
  expectedSignal: string;
  injectCount: number;
  injectType: "cash_in" | "cash_out";
  amountRange: [number, number];
  timeSpreadMs: number;
}

export const PREDEFINED_SCENARIOS: ScenarioDefinition[] = [
  {
    id: "hidden-shortage",
    title: "Hidden Provider Shortage",
    description: "Injects a massive spike in cash-outs to simulate an immediate drain on a specific provider's balance.",
    affectedProvider: "bKash",
    expectedSignal: "Liquidity Pressure, Estimated Shortage Time",
    injectCount: 15,
    injectType: "cash_out",
    amountRange: [15000, 25000],
    timeSpreadMs: 10 * 60 * 1000 // 10 minutes spread
  },
  {
    id: "eid-spike",
    title: "Eid Demand Spike",
    description: "Simulates massive coordinated cash-in activity typical of pre-holiday salary disbursements.",
    affectedProvider: "Rocket",
    expectedSignal: "Velocity Spike (Safe)",
    injectCount: 20,
    injectType: "cash_in",
    amountRange: [5000, 10000],
    timeSpreadMs: 15 * 60 * 1000
  },
  {
    id: "repeated-amount",
    title: "Repeated Amount Cluster",
    description: "Injects multiple identical high-value transactions in a very short window.",
    affectedProvider: "Nagad",
    expectedSignal: "Repeated Amount Cluster Alert",
    injectCount: 5,
    injectType: "cash_out",
    amountRange: [15000, 15000], // exact identical
    timeSpreadMs: 2 * 60 * 1000
  }
];
