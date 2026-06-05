package com.project.taskscheduler.model;

public enum TaskStatus {
    CREATED("Created"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    ERROR("Error");


    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
