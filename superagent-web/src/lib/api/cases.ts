import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";
import { OperationalCasePageResponse, OperationalCaseDetail, CaseStatus, CaseCreationSource } from "../../domain/models";

export async function getCases(
  agentCode: string,
  filters?: {
    providerCode?: string;
    status?: CaseStatus;
    source?: CaseCreationSource;
    page?: number;
    size?: number;
  }
): Promise<OperationalCasePageResponse> {
  const params = new URLSearchParams();
  params.append("agentCode", agentCode);

  if (filters?.providerCode) params.append("providerCode", filters.providerCode);
  if (filters?.status) params.append("status", filters.status);
  if (filters?.source) params.append("creationSource", filters.source);
  if (filters?.page !== undefined) params.append("page", filters.page.toString());
  if (filters?.size !== undefined) params.append("size", filters.size.toString());

  const response = await fetch(`${API_BASE_URL}/api/v1/cases?${params.toString()}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  return handleApiResponse<OperationalCasePageResponse>(response);
}

export async function getCaseDetail(caseCode: string): Promise<OperationalCaseDetail> {
  const response = await fetch(`${API_BASE_URL}/api/v1/cases/${caseCode}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  return handleApiResponse<OperationalCaseDetail>(response);
}
