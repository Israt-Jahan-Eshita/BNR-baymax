import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";

export interface TransactionSummaryResponse {
  transactionReference: string;
  agentCode: string;
  providerCode: string;
  providerDisplayName: string;
  type: string;
  amount: number;
  syntheticAccountId: string;
  scenarioRunId: string | null;
  source: string;
  occurredAt: string;
}

export interface TransactionPageResponse {
  transactions: TransactionSummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export async function getTransactions(
  agentCode: string,
  filters?: {
    providerCode?: string;
    page?: number;
    size?: number;
  }
): Promise<TransactionPageResponse> {
  const params = new URLSearchParams();
  params.append("agentCode", agentCode);

  if (filters?.providerCode) params.append("providerCode", filters.providerCode);
  if (filters?.page !== undefined) params.append("page", filters.page.toString());
  if (filters?.size !== undefined) params.append("size", filters.size.toString());

  const response = await fetch(`${API_BASE_URL}/api/v1/transactions?${params.toString()}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  return handleApiResponse<TransactionPageResponse>(response);
}
