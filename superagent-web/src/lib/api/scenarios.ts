import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";
import { 
  ScenarioDefinition, 
  ScenarioType, 
  ScenarioRunDetail, 
  ScenarioRunPageResponse, 
  ScenarioRunStatus 
} from "../../domain/models";

export async function getScenarioDefinitions(): Promise<ScenarioDefinition[]> {
  const response = await fetch(`${API_BASE_URL}/api/v1/scenarios/definitions`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });
  return handleApiResponse<ScenarioDefinition[]>(response);
}

export async function runScenario(scenarioType: ScenarioType, agentCode: string): Promise<ScenarioRunDetail> {
  const params = new URLSearchParams();
  params.append("agentCode", agentCode);

  const response = await fetch(`${API_BASE_URL}/api/v1/scenarios/${scenarioType}/run?${params.toString()}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });
  return handleApiResponse<ScenarioRunDetail>(response);
}

export async function getScenarioRuns(
  agentCode: string,
  filters?: {
    scenarioType?: ScenarioType;
    status?: ScenarioRunStatus;
    page?: number;
    size?: number;
  }
): Promise<ScenarioRunPageResponse> {
  const params = new URLSearchParams();
  params.append("agentCode", agentCode);

  if (filters?.scenarioType) params.append("scenarioType", filters.scenarioType);
  if (filters?.status) params.append("status", filters.status);
  if (filters?.page !== undefined) params.append("page", filters.page.toString());
  if (filters?.size !== undefined) params.append("size", filters.size.toString());

  const response = await fetch(`${API_BASE_URL}/api/v1/scenarios/runs?${params.toString()}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  return handleApiResponse<ScenarioRunPageResponse>(response);
}

export async function getScenarioRunDetail(scenarioRunId: string): Promise<ScenarioRunDetail> {
  const response = await fetch(`${API_BASE_URL}/api/v1/scenarios/runs/${scenarioRunId}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });
  return handleApiResponse<ScenarioRunDetail>(response);
}
