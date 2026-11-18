package com.jobprocessing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "system_settings")
public class SystemSettings {

    @Id
    private String key;

    private Object value;

    private String description;

    private boolean requiresRestart;

    private Instant updatedAt;

    public SystemSettings() {
        this.updatedAt = Instant.now();
    }

    public SystemSettings(String key, Object value, String description, boolean requiresRestart) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.requiresRestart = requiresRestart;
        this.updatedAt = Instant.now();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.updatedAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequiresRestart() {
        return requiresRestart;
    }

    public void setRequiresRestart(boolean requiresRestart) {
        this.requiresRestart = requiresRestart;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
