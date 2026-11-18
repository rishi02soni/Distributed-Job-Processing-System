package com.jobprocessing.dto;

import com.jobprocessing.model.JobPriority;
import com.jobprocessing.model.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CreateJobRequest {

    @NotBlank(message = "Job name is required")
    @Size(max = 120, message = "Job name cannot exceed 120 characters")
    private String name;

    @NotNull(message = "Job type is required")
    private JobType type;

    private JobPriority priority = JobPriority.NORMAL;

    private Map<String, Object> payload = new HashMap<>();

    private String idempotencyKey;

    private Integer maxRetries;

    private Integer timeoutSeconds;

    private Instant scheduledAt;

    public CreateJobRequest() {
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

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}
