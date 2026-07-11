export class ApiError extends Error {
  status: number;
  responseBody?: unknown;

  constructor(status: number, message: string, responseBody?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.responseBody = responseBody;
  }
}

export async function handleApiResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = response.statusText;
    let responseBody: unknown;

    try {
      responseBody = await response.json();
      if (
        responseBody &&
        typeof responseBody === "object" &&
        "message" in responseBody &&
        typeof (responseBody as any).message === "string"
      ) {
        errorMessage = (responseBody as any).message;
      } else if (
        responseBody &&
        typeof responseBody === "object" &&
        "error" in responseBody &&
        typeof (responseBody as any).error === "string"
      ) {
        errorMessage = (responseBody as any).error;
      }
    } catch {
      // Failed to parse JSON, stick with statusText
    }

    throw new ApiError(response.status, errorMessage, responseBody);
  }

  // Handle empty responses (like 204 No Content)
  const text = await response.text();
  if (!text) {
    return {} as T;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    throw new Error("Failed to parse successful response JSON");
  }
}
