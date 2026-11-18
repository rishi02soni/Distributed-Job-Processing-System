package com.jobprocessing.exception;

public class IdempotencyConflictException extends RuntimeException {
    private final String existingJobId;

    public IdempotencyConflictException(String message, String existingJobId) {
        super(message);
        this.existingJobId = existingJobId;
    }

    public String getExistingJobId() {
        return existingJobId;
    }
}
