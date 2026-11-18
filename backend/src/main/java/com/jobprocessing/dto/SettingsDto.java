package com.jobprocessing.dto;

public class SettingsDto {

    private int defaultMaxRetries;
    private long backoffBaseMs;
    private double backoffMultiplier;
    private int jobTimeoutSeconds;
    private int workerConcurrency;
    private long streamRetentionMaxLen;
    private int dashboardRefreshIntervalMs;

    public SettingsDto() {
    }

    public int getDefaultMaxRetries() {
        return defaultMaxRetries;
    }

    public void setDefaultMaxRetries(int defaultMaxRetries) {
        this.defaultMaxRetries = defaultMaxRetries;
    }

    public long getBackoffBaseMs() {
        return backoffBaseMs;
    }

    public void setBackoffBaseMs(long backoffBaseMs) {
        this.backoffBaseMs = backoffBaseMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public int getJobTimeoutSeconds() {
        return jobTimeoutSeconds;
    }

    public void setJobTimeoutSeconds(int jobTimeoutSeconds) {
        this.jobTimeoutSeconds = jobTimeoutSeconds;
    }

    public int getWorkerConcurrency() {
        return workerConcurrency;
    }

    public void setWorkerConcurrency(int workerConcurrency) {
        this.workerConcurrency = workerConcurrency;
    }

    public long getStreamRetentionMaxLen() {
        return streamRetentionMaxLen;
    }

    public void setStreamRetentionMaxLen(long streamRetentionMaxLen) {
        this.streamRetentionMaxLen = streamRetentionMaxLen;
    }

    public int getDashboardRefreshIntervalMs() {
        return dashboardRefreshIntervalMs;
    }

    public void setDashboardRefreshIntervalMs(int dashboardRefreshIntervalMs) {
        this.dashboardRefreshIntervalMs = dashboardRefreshIntervalMs;
    }
}
