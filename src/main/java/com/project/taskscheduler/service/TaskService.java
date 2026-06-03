package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.Task;
import com.project.taskscheduler.repository.implementation.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public Task createTask(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(Task.TaskStatus.ACTIVE);
        }

        task.setActive(task.getStatus() == Task.TaskStatus.ACTIVE);

        if (task.getNextRun() == null) {
            calculateNextRun(task);
        }

        return taskRepository.save(task);
    }

    public Task updateTask(String id, Task updatedTask) {
        Task existingTask = getTaskById(parseUuid(id));

        existingTask.setName(updatedTask.getName());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setType(updatedTask.getType());
        existingTask.setSchedule(updatedTask.getSchedule());

        if (updatedTask.getStatus() != null) {
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setActive(updatedTask.getStatus() == Task.TaskStatus.ACTIVE);
        }

        calculateNextRun(existingTask);

        return taskRepository.save(existingTask);
    }

    public void deleteTask(String id) {
        Task task = getTaskById(parseUuid(id));
        taskRepository.delete(task);
    }

    public Task pauseTask(String id) {
        Task task = getTaskById(parseUuid(id));

        task.setActive(false);
        task.setStatus(Task.TaskStatus.PAUSED);

        return taskRepository.save(task);
    }

    public Task resumeTask(String id) {
        Task task = getTaskById(parseUuid(id));

        task.setActive(true);
        task.setStatus(Task.TaskStatus.ACTIVE);
        calculateNextRun(task);

        return taskRepository.save(task);
    }

    public Task executeTask(String id) {
        Task task = getTaskById(parseUuid(id));

        task.setLastRun(LocalDateTime.now());
        calculateNextRun(task);

        return taskRepository.save(task);
    }

    public List<Task> getActiveTasks() {
        return taskRepository.findByActiveTrue();
    }

    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public Map<String, Object> getTaskStatistics() {
        List<Task> tasks = taskRepository.findAll();

        long activeTasks = tasks.stream()
                .filter(Task::isActive)
                .count();

        long pausedTasks = tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.PAUSED)
                .count();

        long errorTasks = tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.ERROR)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", tasks.size());
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

    private void calculateNextRun(Task task) {
        LocalDateTime now = LocalDateTime.now();

        if (task.getType() == null || task.getSchedule() == null || task.getSchedule().isBlank()) {
            return;
        }

        switch (task.getType()) {
            case FIXED_RATE -> {
                long interval = Long.parseLong(task.getSchedule());
                task.setNextRun(now.plus(Duration.ofMillis(interval)));
            }
            case FIXED_DELAY -> {
                long delay = Long.parseLong(task.getSchedule());
                task.setNextRun(now.plus(Duration.ofMillis(delay)));
            }
            case CRON -> task.setNextRun(now.plusMinutes(1));
        }
    }
}