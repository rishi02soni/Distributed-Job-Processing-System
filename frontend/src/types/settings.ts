export interface SystemSettings {
  defaultMaxRetries: number;
  backoffBaseMs: number;
  backoffMultiplier: number;
  jobTimeoutSeconds: number;
  workerConcurrency: number;
  streamRetentionMaxLen: number;
  dashboardRefreshIntervalMs: number;
}
