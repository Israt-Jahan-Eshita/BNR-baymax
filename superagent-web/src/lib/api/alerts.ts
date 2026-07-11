import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";
import { AlertPageResponse, AlertDetail, AlertType, AlertSeverity } from "../../domain/models";

export async function getAlerts(
  agentCode: string,
  filters?: {
    providerCode?: string;
    type?: AlertType;
    severity?: AlertSeverity;
    page?: number;
    size?: number;
  }
): Promise<AlertPageResponse> {
  const params = new URLSearchParams();
  params.append("agentCode", agentCode);

  if (filters?.providerCode) params.append("providerCode", filters.providerCode);
  if (filters?.type) params.append("alertType", filters.type);
  if (filters?.severity) params.append("severity", filters.severity);
  if (filters?.page !== undefined) params.append("page", filters.page.toString());
  if (filters?.size !== undefined) params.append("size", filters.size.toString());

  const response = await fetch(`${API_BASE_URL}/api/v1/alerts?${params.toString()}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  return handleApiResponse<AlertPageResponse>(response);
}

export async function getAlertDetail(alertCode: string): Promise<AlertDetail> {
  const response = await fetch(`${API_BASE_URL}/api/v1/alerts/${alertCode}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  return handleApiResponse<AlertDetail>(response);
}
