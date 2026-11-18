package com.jobprocessing.dto;

import com.jobprocessing.model.WorkerNode;
import com.jobprocessing.model.WorkerStatus;

import java.time.Duration;
import java.time.Instant;

public class WorkerResponse {

    private String workerId;
    private String hostname;
    private WorkerStatus status;
    private int activeJobs;
    private long completedJobs;
    private long failedJobs;
    private int concurrency;
    private Instant lastHeartbeat;
    private double cpuUsage;
    private double memoryUsage;
    private Instant startedAt;
    private boolean healthy;
    private long secondsSinceHeartbeat;

    public WorkerResponse() {
    }

    public static WorkerResponse fromEntity(WorkerNode node, int timeoutSeconds) {
        WorkerResponse resp = new WorkerResponse();
        resp.setWorkerId(node.getWorkerId());
        resp.setHostname(node.getHostname());
        resp.setStatus(node.getStatus());
        resp.setActiveJobs(node.getActiveJobs());
        resp.setCompletedJobs(node.getCompletedJobs());
        resp.setFailedJobs(node.getFailedJobs());
        resp.setConcurrency(node.getConcurrency());
        resp.setLastHeartbeat(node.getLastHeartbeat());
        resp.setCpuUsage(node.getCpuUsage());
        resp.setMemoryUsage(node.getMemoryUsage());
        resp.setStartedAt(node.getStartedAt());

        long secondsSince = node.getLastHeartbeat() != null ?
                Duration.between(node.getLastHeartbeat(), Instant.now()).toSeconds() : 9999;
        resp.setSecondsSinceHeartbeat(secondsSince);
        resp.setHealthy(secondsSince < timeoutSeconds && node.getStatus() != WorkerStatus.DEAD);
        return resp;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void setStatus(WorkerStatus status) {
        this.status = status;
    }

    public int getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(int activeJobs) {
        this.activeJobs = activeJobs;
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

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public long getSecondsSinceHeartbeat() {
        return secondsSinceHeartbeat;
    }

    public void setSecondsSinceHeartbeat(long secondsSinceHeartbeat) {
        this.secondsSinceHeartbeat = secondsSinceHeartbeat;
    }
}
