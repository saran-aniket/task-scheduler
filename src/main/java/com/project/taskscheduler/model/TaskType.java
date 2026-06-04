package com.project.taskscheduler.model;

public enum TaskType {
    FIXED_RATE("Fixed Rate"),
    FIXED_DELAY("Fixed Delay"),
    CRON("Cron Expression");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
