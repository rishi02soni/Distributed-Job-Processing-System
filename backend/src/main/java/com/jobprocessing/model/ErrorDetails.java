package com.jobprocessing.model;

import java.time.Instant;

public class ErrorDetails {
    private String message;
    private String errorType;
    private String stackTrace;
    private Instant failedAt;

    public ErrorDetails() {
    }

    public ErrorDetails(String message, String errorType, String stackTrace, Instant failedAt) {
        this.message = message;
        this.errorType = errorType;
        this.stackTrace = stackTrace;
        this.failedAt = failedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }
}
