export interface ReportRequest {
  userInput: string;
}


export interface ReportResponse {
  id: string;
  reportResult: string;
  status: TaskStatus;
  createAt: string // double check with backend type
}

export type TaskStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
