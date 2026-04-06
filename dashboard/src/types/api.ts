export interface Job {
  id: string;
  status: 'PENDING' | 'ASSIGNED' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  payload: Record<string, unknown>;
  result: string | null;
  workerId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Worker {
  id: string;
  hostname: string;
  ipAddress: string;
  status: 'IDLE' | 'BUSY' | 'OFFLINE';
  lastHeartbeat: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface CreateJobRequest {
  payload: Record<string, unknown>;
}

export interface ApiError {
  message: string;
  status?: number;
  code?: string;
  data?: unknown;
}
