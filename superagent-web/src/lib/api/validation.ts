import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";
import { ValidationMetricsResponse } from "../../domain/models";

export async function getValidationMetrics(): Promise<ValidationMetricsResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/validation/metrics`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });
  return handleApiResponse<ValidationMetricsResponse>(response);
}
