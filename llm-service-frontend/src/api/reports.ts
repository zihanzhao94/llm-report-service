// Report API functions: submit and get report from backend
import { apiClient } from "./client";
import type { ReportRequest, ReportResponse } from "../types";


// Submit report to backend (async function, doesn't block network connection)
export const submitReport = async (request: ReportRequest): Promise<ReportResponse> => {
  const response = await apiClient.post<ReportResponse>('/api/reports', request);
  return response.data;
}

// Get report from backend (async function, doesn't block network connection)
export const getReport = async (taskId: string): Promise<ReportResponse> => {
  const response = await apiClient.get<ReportResponse>(`/api/reports/${taskId}`);
  return response.data;
}