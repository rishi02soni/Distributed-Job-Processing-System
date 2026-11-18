export interface ThroughputPoint {
  timestamp: string;
  completed: number;
  failed: number;
}

export interface LatencyMetrics {
  p50: number;
  p90: number;
  p99: number;
  avg: number;
  min: number;
  max: number;
}

export interface WorkerPerformance {
  workerId: string;
  jobsCompleted: number;
  jobsFailed: number;
  avgDurationMs: number;
  successRate: number;
}

export interface QueueGrowthPoint {
  time: string;
  depth: number;
}

export interface AnalyticsData {
  throughputOverTime: ThroughputPoint[];
  latency: LatencyMetrics;
  jobTypeDistribution: Record<string, number>;
  failureReasonDistribution: Record<string, number>;
  workerPerformance: WorkerPerformance[];
  queueGrowth: QueueGrowthPoint[];
}
