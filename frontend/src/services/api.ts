import type {
  Job,
  JobLog,
  JobStats,
  CreateJobInput,
  PagedResponse,
  ApiResponse,
  JobStatus,
  JobType,
  JobPriority
} from '../types/job';
import type { WorkerNode } from '../types/worker';
import type { QueueStats } from '../types/queue';
import type { AnalyticsData } from '../types/analytics';
import type { SystemSettings } from '../types/settings';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    let errorMsg = `HTTP Error ${response.status}: ${response.statusText}`;
    try {
      const errJson = await response.json();
      if (errJson.message) errorMsg = errJson.message;
      if (errJson.validationErrors) {
        const details = Object.entries(errJson.validationErrors)
          .map(([f, m]) => `${f}: ${m}`)
          .join(', ');
        errorMsg += ` (${details})`;
      }
    } catch (_) {}
    throw new Error(errorMsg);
  }

  const json: ApiResponse<T> = await response.json();
  return json.data;
}

export const api = {
  // Jobs
  async getJobs(params?: {
    status?: JobStatus;
    type?: JobType;
    priority?: JobPriority;
    search?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PagedResponse<Job>> {
    const query = new URLSearchParams();
    if (params?.status) query.set('status', params.status);
    if (params?.type) query.set('type', params.type);
    if (params?.priority) query.set('priority', params.priority);
    if (params?.search) query.set('search', params.search);
    if (params?.startDate) query.set('startDate', params.startDate);
    if (params?.endDate) query.set('endDate', params.endDate);
    if (params?.page !== undefined) query.set('page', params.page.toString());
    if (params?.size !== undefined) query.set('size', params.size.toString());
    if (params?.sortBy) query.set('sortBy', params.sortBy);
    if (params?.sortDir) query.set('sortDir', params.sortDir);

    const queryString = query.toString();
    return request<PagedResponse<Job>>(`/jobs${queryString ? `?${queryString}` : ''}`);
  },

  async getJob(id: string): Promise<Job> {
    return request<Job>(`/jobs/${id}`);
  },

  async getJobLogs(id: string): Promise<JobLog[]> {
    return request<JobLog[]>(`/jobs/${id}/logs`);
  },

  async createJob(input: CreateJobInput): Promise<Job> {
    const headers: Record<string, string> = {};
    if (input.idempotencyKey) {
      headers['Idempotency-Key'] = input.idempotencyKey;
    }
    return request<Job>('/jobs', {
      method: 'POST',
      headers,
      body: JSON.stringify(input),
    });
  },

  async retryJob(id: string): Promise<Job> {
    return request<Job>(`/jobs/${id}/retry`, { method: 'POST' });
  },

  async cancelJob(id: string): Promise<Job> {
    return request<Job>(`/jobs/${id}/cancel`, { method: 'POST' });
  },

  async getJobStats(): Promise<JobStats> {
    return request<JobStats>('/jobs/stats');
  },

  // Workers
  async getWorkers(): Promise<WorkerNode[]> {
    return request<WorkerNode[]>('/workers');
  },

  // Queue
  async getQueueStats(): Promise<QueueStats> {
    return request<QueueStats>('/queue/stats');
  },

  // Analytics
  async getAnalytics(): Promise<AnalyticsData> {
    return request<AnalyticsData>('/analytics');
  },

  // Settings
  async getSettings(): Promise<SystemSettings> {
    return request<SystemSettings>('/settings');
  },

  async updateSettings(settings: SystemSettings): Promise<SystemSettings> {
    return request<SystemSettings>('/settings', {
      method: 'PUT',
      body: JSON.stringify(settings),
    });
  },

  // Health
  async getHealth(): Promise<Record<string, any>> {
    return request<Record<string, any>>('/health');
  },
};
