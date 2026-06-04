package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.repository.TaskRepository;
import com.project.taskscheduler.utility.TaskUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.project.taskscheduler.utility.TaskUtility.parseUuid;

@Service
public class TaskService {

    private final Logger logger = LoggerFactory.getLogger(TaskService.class);
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
        //validate CRON expression
        if (!TaskUtility.validateCronExpression(taskDefinition.getSchedule())) {
            throw new IllegalArgumentException("Invalid cron expression");
        }
        logger.info("Creating task: {}", taskDefinition);
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
            existingTaskDefinition.setActive(updatedTaskDefinition.getStatus() == TaskStatus.ACTIVE);
        }

        existingTaskDefinition.setNextRun(TaskUtility.getNextRunFromCron(existingTaskDefinition.getSchedule()));
        logger.info("Updating task: {}", existingTaskDefinition);
        return taskRepository.save(existingTaskDefinition);
    }

    public void deleteTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));
        logger.info("Deleting task: {}", taskDefinition);
        taskRepository.delete(taskDefinition);
    }

    public List<TaskDefinition> getActiveTasks() {
        return taskRepository.findByActiveTrue();
    }

    public List<TaskDefinition> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public Map<String, Object> getTaskStatistics() {
        List<TaskDefinition> taskDefinitions = taskRepository.findAll();

        long activeTasks = taskDefinitions.stream()
                .filter(TaskDefinition::isActive)
                .count();

        long pausedTasks = taskDefinitions.stream()
                .filter(task -> task.getStatus() == TaskStatus.PAUSED)
                .count();

        long errorTasks = taskDefinitions.stream()
                .filter(task -> task.getStatus() == TaskStatus.ERROR)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", taskDefinitions.size());
        stats.put("activeTasks", activeTasks);
        stats.put("pausedTasks", pausedTasks);
        stats.put("errorTasks", errorTasks);

        return stats;
    }
}