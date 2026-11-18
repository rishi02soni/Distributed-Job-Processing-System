package com.jobprocessing.dto;

public class JobStatsResponse {

    private long totalJobs;
    private long queuedJobs;
    private long runningJobs;
    private long completedJobs;
    private long failedJobs;
    private long retryingJobs;
    private long cancelledJobs;
    private double successRate;
    private double avgProcessingTimeMs;
    private long activeWorkers;
    private long queueDepth;

    public JobStatsResponse() {
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getQueuedJobs() {
        return queuedJobs;
    }

    public void setQueuedJobs(long queuedJobs) {
        this.queuedJobs = queuedJobs;
    }

    public long getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(long runningJobs) {
        this.runningJobs = runningJobs;
    }

    public long getCompletedJobs() {
        return completedJobs;
    }

    public void setCompletedJobs(long completedJobs) {
        this.completedJobs = completedJobs;
    }

    public long getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(long failedJobs) {
        this.failedJobs = failedJobs;
    }

    public long getRetryingJobs() {
        return retryingJobs;
    }

    public void setRetryingJobs(long retryingJobs) {
        this.retryingJobs = retryingJobs;
    }

    public long getCancelledJobs() {
        return cancelledJobs;
    }

    public void setCancelledJobs(long cancelledJobs) {
        this.cancelledJobs = cancelledJobs;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAvgProcessingTimeMs() {
        return avgProcessingTimeMs;
    }

    public void setAvgProcessingTimeMs(double avgProcessingTimeMs) {
        this.avgProcessingTimeMs = avgProcessingTimeMs;
    }

    public long getActiveWorkers() {
        return activeWorkers;
    }

    public void setActiveWorkers(long activeWorkers) {
        this.activeWorkers = activeWorkers;
    }

    public long getQueueDepth() {
        return queueDepth;
    }

    public void setQueueDepth(long queueDepth) {
        this.queueDepth = queueDepth;
    }
}
