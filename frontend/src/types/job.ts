export type JobStatus = 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'RETRYING' | 'CANCELLED';

export type JobType = 'EMAIL' | 'REPORT' | 'DATA_PROCESSING' | 'CUSTOM';

export type JobPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';

export interface ErrorDetails {
  message: string;
  errorType: string;
  stackTrace?: string;
  failedAt: string;
}

export interface Job {
  id: string;
  name: string;
  type: JobType;
  priority: JobPriority;
  status: JobStatus;
  payload: Record<string, any>;
  result?: Record<string, any>;
  errorDetails?: ErrorDetails;
  attemptCount: number;
  maxRetries: number;
  timeoutSeconds: number;
  idempotencyKey?: string;
  workerId?: string;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  durationMs?: number;
  scheduledAt?: string;
  updatedAt: string;
}

export interface JobLog {
  id: string;
  jobId: string;
  timestamp: string;
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';
  message: string;
  details?: Record<string, any>;
}

export interface JobStats {
  totalJobs: number;
  queuedJobs: number;
  runningJobs: number;
  completedJobs: number;
  failedJobs: number;
  retryingJobs: number;
  cancelledJobs: number;
  successRate: number;
  avgProcessingTimeMs: number;
  activeWorkers: number;
  queueDepth: number;
}

export interface CreateJobInput {
  name: string;
  type: JobType;
  priority?: JobPriority;
  payload?: Record<string, any>;
  idempotencyKey?: string;
  maxRetries?: number;
  timeoutSeconds?: number;
  scheduledAt?: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}
