export type WorkerStatus = 'ACTIVE' | 'BUSY' | 'IDLE' | 'DEAD';

export interface WorkerNode {
  workerId: string;
  hostname: string;
  status: WorkerStatus;
  activeJobs: number;
  completedJobs: number;
  failedJobs: number;
  concurrency: number;
  lastHeartbeat: string;
  cpuUsage: number;
  memoryUsage: number;
  startedAt: string;
  healthy: boolean;
  secondsSinceHeartbeat: number;
}
