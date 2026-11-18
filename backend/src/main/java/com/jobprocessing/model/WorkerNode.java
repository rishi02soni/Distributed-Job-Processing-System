package com.jobprocessing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "workers")
public class WorkerNode {

    @Id
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

    public WorkerNode() {
        this.status = WorkerStatus.IDLE;
        this.startedAt = Instant.now();
        this.lastHeartbeat = Instant.now();
    }

    public WorkerNode(String workerId, String hostname, int concurrency) {
        this.workerId = workerId;
        this.hostname = hostname;
        this.concurrency = concurrency;
        this.status = WorkerStatus.IDLE;
        this.startedAt = Instant.now();
        this.lastHeartbeat = Instant.now();
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
}
