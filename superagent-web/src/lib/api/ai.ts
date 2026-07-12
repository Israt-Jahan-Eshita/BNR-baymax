import { API_BASE_URL } from "./config";
import { handleApiResponse } from "./api-error";

export interface AiChatRequest {
  agentCode: string;
  question: string;
  language?: string;
  persona?: string;
}

export interface AiChatResponse {
  answer: string;
}

export interface BaymaxResponse {
  answer: string;
  confidence: "HIGH" | "MEDIUM" | "LOW";
  evidences: string[];
  anomaliesDetected: string[];
  whatIfProjections: string[];
  recommendedActions: string[];
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

export async function analyzeWithBaymax(request: AiChatRequest): Promise<BaymaxResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/ai/analyze`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
    cache: "no-store",
  });
  return handleApiResponse<BaymaxResponse>(response);
}

export async function transcribeAudio(audioBlob: Blob): Promise<string> {
  const formData = new FormData();
  formData.append("file", audioBlob, "recording.webm");

  const response = await fetch(`${API_BASE_URL}/api/v1/ai/transcribe`, {
    method: "POST",
    body: formData,
  });
  const data = await handleApiResponse<{ text: string }>(response);
  return data.text;
}

export async function generateSpeechUrl(text: string): Promise<string> {
  const response = await fetch(`${API_BASE_URL}/api/v1/ai/speech`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text }),
  });
  if (!response.ok) {
    throw new Error("Failed to generate speech");
  }
  const blob = await response.blob();
  return URL.createObjectURL(blob);
}
