package com.project.taskscheduler.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_executions")
public class TaskExecution extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    private TaskDefinition taskDefinition;
    private String executionType;
    @Enumerated(EnumType.STRING)
    private TaskExecutionStatus taskExecutionStatus;
    private LocalDateTime executionStartTime;
    private LocalDateTime executionEndTime;
    private long executionDuration;
    private int attemptCount;
    private String errorMessage;

    public TaskExecution() {

    }

    public TaskExecution(TaskDefinition taskDefinition, String executionType, LocalDateTime executionStartTime, long executionDuration) {
        this.taskDefinition = taskDefinition;
        this.executionType = executionType;
        this.executionStartTime = executionStartTime;
        this.executionDuration = executionDuration;
    }


    // Getters and Setters
    public TaskDefinition getTaskDefinition() {
        return taskDefinition;
    }

    public void setTaskDefinition(TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public LocalDateTime getExecutionStartTime() {
        return executionStartTime;
    }

    public void setExecutionStartTime(LocalDateTime executionStartTime) {
        this.executionStartTime = executionStartTime;
    }

    public LocalDateTime getExecutionEndTime() {
        return executionEndTime;
    }

    public void setExecutionEndTime(LocalDateTime executionEndTime) {
        this.executionEndTime = executionEndTime;
    }

    public long getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TaskExecutionStatus getTaskExecutionStatus() {
        return taskExecutionStatus;
    }

    public void setTaskExecutionStatus(TaskExecutionStatus taskExecutionStatus) {
        this.taskExecutionStatus = taskExecutionStatus;
    }

}
