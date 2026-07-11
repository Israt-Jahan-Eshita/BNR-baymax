import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";
import { DashboardAggregateResponse } from "../../domain/models";

export async function getDashboard(agentCode: string): Promise<DashboardAggregateResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/dashboard/aggregate?agentCode=${agentCode}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
    // We can use no-store to ensure we always get fresh data from the dashboard
    cache: "no-store",
  });
  return handleApiResponse<DashboardAggregateResponse>(response);
}
