package com.project.taskscheduler.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_definitions")
public class Task extends BaseModel {

    @NotBlank(message = "Task name is required")
    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull(message = "Task type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @NotBlank(message = "Task schedule is required")
    @Column(nullable = false)
    private String schedule;

    private boolean active;

    private LocalDateTime lastRun;

    private LocalDateTime nextRun;

    @NotNull(message = "Task status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    public Task() {
        this.status = TaskStatus.ACTIVE;
        this.active = true;
    }

    public Task(String name, String description, TaskType type, String schedule) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.schedule = schedule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public LocalDateTime getNextRun() {
        return nextRun;
    }

    public void setNextRun(LocalDateTime nextRun) {
        this.nextRun = nextRun;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", schedule='" + schedule + '\'' +
                ", active=" + active +
                ", status=" + status +
                '}';
    }

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

    public enum TaskStatus {
        ACTIVE("Active"),
        PAUSED("Paused"),
        ERROR("Error");

        private final String displayName;

        TaskStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}