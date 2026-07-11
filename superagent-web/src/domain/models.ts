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

// --- NEW BACKEND ALIGNED TYPES ---
export type AlertSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type AlertType = "CASH_OUT_VELOCITY_SPIKE" | "REPEATED_AMOUNT_CLUSTER";
export type SignalConfidence = "LOW" | "MEDIUM" | "HIGH";

export interface AlertSummary {
  alertCode: string;
  agentCode: string;
  providerCode: string | null;
  providerDisplayName: string | null;
  alertType: string;
  severity: AlertSeverity;
  confidence: SignalConfidence;
  confidenceScore: number;
  title: string;
  summary: string;
  detectedAt: string;
  mlReviewProbability?: number | null;
  mlRequiresReview?: boolean | null;
  mlModelVersion?: string | null;
}

export interface AlertDetail extends AlertSummary {
  evidence: string[];
  possibleNormalExplanation: string;
  uncertainty: string;
  safeNextStep: string;
  windowStart: string;
  windowEnd: string;
  createdAt: string;
  mlSelectedThreshold?: number | null;
  eventContextSummary?: string | null;
}

export interface AlertPageResponse {
  alerts: AlertSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export type ScenarioType = "HIDDEN_PROVIDER_SHORTAGE" | "EVENT_DEMAND_SPIKE" | "REPEATED_AMOUNT_CLUSTER" | "PROVIDER_FEED_DELAY" | "CONFLICTING_BALANCE_DATA" | "CASH_OUT_VELOCITY_SPIKE" | "NORMAL";
export type ScenarioRunStatus = "RUNNING" | "COMPLETED" | "FAILED";

export interface ScenarioDefinition {
  scenarioType: ScenarioType;
  displayName: string;
  description: string;
  expectedSystemEffect: string;
}

export interface ScenarioRunSummary {
  scenarioRunId: string;
  scenarioType: ScenarioType;
  agentCode: string;
  status: ScenarioRunStatus;
  committedTransactionCount: number;
  summary: string;
  startedAt: string;
  completedAt?: string;
}

export interface ScenarioRunDetail extends ScenarioRunSummary {
  agentDisplayName: string;
  failureMessage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ScenarioRunPageResponse {
  runs: ScenarioRunSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export type ProviderDataHealthStatus = "LIVE" | "DELAYED" | "MISSING" | "CONFLICTING";

export interface ProviderDataHealthResponse {
  agentCode: string;
  providerCode: string;
  providerDisplayName: string;
  status: ProviderDataHealthStatus;
  lastSuccessfulUpdateAt?: string;
  delayMinutes: number;
  conflictDescription?: string;
  updatedAt: string;
}

export interface AgentBalanceResponse {
  agentCode: string;
  physicalCashBalance: number;
  providerBalances: ProviderBalanceRecord[];
  totalEMoneyBalance: number;
}

export interface ProviderBalanceRecord {
  providerCode: string;
  providerDisplayName: string;
  eMoneyBalance: number;
}

export interface LiquidityResourceForecast {
  resourceType: string;
  providerCode: string | null;
  resourceDisplayName: string;
  currentBalance: number;
  rate15: number;
  rate30: number;
  rate60: number;
  weightedConsumptionPerMinute: number;
  projectedRunwayMinutes: number | null;
  estimatedShortageAt: string | null;
  status: string;
  confidence: string;
  confidenceScore: number;
  recentTransactionCount: number;
  explanation: string;
  dataHealthStatus?: ProviderDataHealthStatus;
  uncertainty?: string;
}

export interface LiquidityForecastResponse {
  agentCode: string;
  forecasts: LiquidityResourceForecast[];
  forecastedAt: string;
}

export type CaseCreationSource = "AUTO_ALERT_POLICY" | "MANUAL_OPERATOR";
export type CasePriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface OperationalCaseSummary {
  caseCode: string;
  creationSource: CaseCreationSource;
  sourceAlertCode: string | null;
  agentCode: string;
  providerCode: string | null;
  providerDisplayName: string | null;
  priority: CasePriority;
  status: CaseStatus;
  title: string;
  openedAt: string;
}

export interface CaseAuditRecord {
  action: string;
  actorType: string;
  actorReference: string;
  details: string;
  occurredAt: string;
}

export interface OperationalCaseDetail extends OperationalCaseSummary {
  agentDisplayName: string;
  description: string;
  recommendedNextStep: string;
  createdAt: string;
  updatedAt: string;
  auditTrail: CaseAuditRecord[];
}

export interface OperationalCasePageResponse {
  cases: OperationalCaseSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ValidationMetricsResponse {
  start: string;
  end: string;
  evaluatedScenarioCount: number;
  truePositiveCount: number;
  trueNegativeCount: number;
  falsePositiveCount: number;
  falseNegativeCount: number;
  precision: number;
  recall: number;
  falsePositiveRate: number;
  accuracy: number;
  averageDetectionLatencyMilliseconds: number;
  validationScope: string;
  interpretation: string;
}

export interface DashboardAggregateResponse {
  agentCode: string;
  balances: AgentBalanceResponse;
  forecast: LiquidityForecastResponse;
  dataHealth: ProviderDataHealthResponse[];
  recentAlerts: AlertPageResponse;
  recentCases: OperationalCasePageResponse;
  recentScenarioRuns: ScenarioRunPageResponse;
  aggregatedAt: string;
}

