package com.jobprocessing.model;

public enum JobPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    JobPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
