package com.jobprocessing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "job_logs")
@CompoundIndex(name = "jobId_timestamp_idx", def = "{'jobId': 1, 'timestamp': 1}")
public class JobLog {

    @Id
    private String id;

    @Indexed
    private String jobId;

    private Instant timestamp;

    private String level; // INFO, WARN, ERROR, DEBUG

    private String message;

    private Map<String, Object> details = new HashMap<>();

    public JobLog() {
        this.timestamp = Instant.now();
    }

    public JobLog(String jobId, String level, String message, Map<String, Object> details) {
        this.jobId = jobId;
        this.timestamp = Instant.now();
        this.level = level;
        this.message = message;
        if (details != null) {
            this.details = details;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
