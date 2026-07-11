import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";

export interface AiChatRequest {
  agentCode: string;
  question: string;
}

export interface AiChatResponse {
  answer: string;
}

export async function askAi(request: AiChatRequest): Promise<AiChatResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/ai/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
    cache: "no-store",
  });
  return handleApiResponse<AiChatResponse>(response);
}
