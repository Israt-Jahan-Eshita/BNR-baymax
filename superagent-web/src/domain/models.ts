export type ProviderId = "bKash" | "Nagad" | "Rocket" | "PhysicalCash";
export type CaseStatus = "OPEN" | "ACKNOWLEDGED" | "ASSIGNED" | "ESCALATED" | "RESOLVED";
export type Severity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type ProviderHealthState = "LIVE" | "DELAYED" | "MISSING" | "CONFLICTING";
export type PressureStatus = "STABLE" | "MEDIUM PRESSURE" | "HIGH PRESSURE" | "DATA UNCERTAIN";

export interface ProviderHealth {
  providerId: ProviderId;
  state: ProviderHealthState;
  lastUpdatedMs: number;
}

export interface OperationalCase {
  id: string;
  providerId: ProviderId;
  title: string;
  caseType: string;
  severity: Severity;
  status: CaseStatus;
  receivedBy: string;
  owner?: string;
  recommendedNextStep: string;
  createdAt: string;
  updatedAt: string;
  confidence: number;
  whyFlagged: string[];
  possibleNormalExplanation: string;
  uncertaintyNotice: string;
  evidence: string[];
}

export interface AuditEvent {
  id: string;
  caseId: string;
  actorId: string;
  action: "CREATED" | "ACKNOWLEDGED" | "ASSIGNED" | "ESCALATED" | "NOTE_ADDED" | "RESOLVED";
  timestamp: string;
  metadata?: Record<string, any>;
  note?: string;
}

export interface Transaction {
  id: string;
  provider: ProviderId;
  type: "cash_in" | "cash_out" | "transfer";
  amount: number;
  account: string;
  time: string;
  timestampMs: number;
}

export interface Forecast {
  providerId: ProviderId;
  currentBalance: number;
  netOutflowRate: number; // per minute
  projectedRunwayMin: number;
  expectedShortageTime: string | null;
  confidence: number;
  pressure: PressureStatus;
}
