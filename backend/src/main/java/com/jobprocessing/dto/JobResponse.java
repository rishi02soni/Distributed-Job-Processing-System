package com.jobprocessing.dto;

import com.jobprocessing.model.ErrorDetails;
import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobPriority;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.model.JobType;

import java.time.Instant;
import java.util.Map;

public class JobResponse {

    private String id;
    private String name;
    private JobType type;
    private JobPriority priority;
    private JobStatus status;
    private Map<String, Object> payload;
    private Map<String, Object> result;
    private ErrorDetails errorDetails;
    private int attemptCount;
    private int maxRetries;
    private int timeoutSeconds;
    private String idempotencyKey;
    private String workerId;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationMs;
    private Instant scheduledAt;
    private Instant updatedAt;

    public JobResponse() {
    }

    public static JobResponse fromEntity(Job job) {
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setName(job.getName());
        resp.setType(job.getType());
        resp.setPriority(job.getPriority());
        resp.setStatus(job.getStatus());
        resp.setPayload(job.getPayload());
        resp.setResult(job.getResult());
        resp.setErrorDetails(job.getErrorDetails());
        resp.setAttemptCount(job.getAttemptCount());
        resp.setMaxRetries(job.getMaxRetries());
        resp.setTimeoutSeconds(job.getTimeoutSeconds());
        resp.setIdempotencyKey(job.getIdempotencyKey());
        resp.setWorkerId(job.getWorkerId());
        resp.setCreatedAt(job.getCreatedAt());
        resp.setStartedAt(job.getStartedAt());
        resp.setCompletedAt(job.getCompletedAt());
        resp.setDurationMs(job.getDurationMs());
        resp.setScheduledAt(job.getScheduledAt());
        resp.setUpdatedAt(job.getUpdatedAt());
        return resp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public JobPriority getPriority() {
        return priority;
    }

    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
