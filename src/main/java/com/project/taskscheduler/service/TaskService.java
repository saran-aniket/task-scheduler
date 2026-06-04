package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.repository.implementation.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskDefinition> getAllTasks() {
        return taskRepository.findAll();
    }

    public TaskDefinition getTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public TaskDefinition createTask(TaskDefinition taskDefinition) {
        if (taskDefinition.getStatus() == null) {
            taskDefinition.setStatus(TaskDefinition.TaskStatus.ACTIVE);
        }

        taskDefinition.setActive(taskDefinition.getStatus() == TaskDefinition.TaskStatus.ACTIVE);

        if (taskDefinition.getNextRun() == null) {
            calculateNextRun(taskDefinition);
        }

        return taskRepository.save(taskDefinition);
    }

    public TaskDefinition updateTask(String id, TaskDefinition updatedTaskDefinition) {
        TaskDefinition existingTaskDefinition = getTaskById(parseUuid(id));

        existingTaskDefinition.setName(updatedTaskDefinition.getName());
        existingTaskDefinition.setDescription(updatedTaskDefinition.getDescription());
        existingTaskDefinition.setType(updatedTaskDefinition.getType());
        existingTaskDefinition.setSchedule(updatedTaskDefinition.getSchedule());

        if (updatedTaskDefinition.getStatus() != null) {
            existingTaskDefinition.setStatus(updatedTaskDefinition.getStatus());
            existingTaskDefinition.setActive(updatedTaskDefinition.getStatus() == TaskDefinition.TaskStatus.ACTIVE);
        }

        calculateNextRun(existingTaskDefinition);

        return taskRepository.save(existingTaskDefinition);
    }

    public void deleteTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));
        taskRepository.delete(taskDefinition);
    }

    public TaskDefinition pauseTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskDefinition.TaskStatus.PAUSED);

        return taskRepository.save(taskDefinition);
    }

    public TaskDefinition resumeTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(true);
        taskDefinition.setStatus(TaskDefinition.TaskStatus.ACTIVE);
        calculateNextRun(taskDefinition);

        return taskRepository.save(taskDefinition);
    }

    public TaskDefinition executeTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setLastRun(LocalDateTime.now());
        calculateNextRun(taskDefinition);

        return taskRepository.save(taskDefinition);
    }

    public List<TaskDefinition> getActiveTasks() {
        return taskRepository.findByActiveTrue();
    }

    public List<TaskDefinition> getTasksByStatus(TaskDefinition.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public Map<String, Object> getTaskStatistics() {
        List<TaskDefinition> taskDefinitions = taskRepository.findAll();

        long activeTasks = taskDefinitions.stream()
                .filter(TaskDefinition::isActive)
                .count();

        long pausedTasks = taskDefinitions.stream()
                .filter(task -> task.getStatus() == TaskDefinition.TaskStatus.PAUSED)
                .count();

        long errorTasks = taskDefinitions.stream()
                .filter(task -> task.getStatus() == TaskDefinition.TaskStatus.ERROR)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", taskDefinitions.size());
        stats.put("activeTasks", activeTasks);
        stats.put("pausedTasks", pausedTasks);
        stats.put("errorTasks", errorTasks);

        return stats;
    }

    private UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new TaskNotFoundException("Invalid task id: " + id);
        }
    }

    private void calculateNextRun(TaskDefinition taskDefinition) {
        LocalDateTime now = LocalDateTime.now();

        if (taskDefinition.getType() == null || taskDefinition.getSchedule() == null || taskDefinition.getSchedule().isBlank()) {
            return;
        }

        switch (taskDefinition.getType()) {
            case FIXED_RATE -> {
                long interval = Long.parseLong(taskDefinition.getSchedule());
                taskDefinition.setNextRun(now.plus(Duration.ofMillis(interval)));
            }
            case FIXED_DELAY -> {
                long delay = Long.parseLong(taskDefinition.getSchedule());
                taskDefinition.setNextRun(now.plus(Duration.ofMillis(delay)));
            }
            case CRON -> taskDefinition.setNextRun(now.plusMinutes(1));
        }
    }
}