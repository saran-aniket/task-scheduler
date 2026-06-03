package com.project.taskscheduler.controller;

import com.project.taskscheduler.model.Task;
import com.project.taskscheduler.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(UUID.fromString(id));
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String id,
            @Valid @RequestBody Task task
    ) {
        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Task> pauseTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.pauseTask(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Task> resumeTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.resumeTask(id));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Task> executeTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.executeTask(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Task>> getActiveTasks() {
        return ResponseEntity.ok(taskService.getActiveTasks());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable String status) {
        Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(taskService.getTasksByStatus(taskStatus));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        return ResponseEntity.ok(taskService.getTaskStatistics());
    }

    @GetMapping("/types")
    public ResponseEntity<Task.TaskType[]> getTaskTypes() {
        return ResponseEntity.ok(Task.TaskType.values());
    }

    @GetMapping("/statuses")
    public ResponseEntity<Task.TaskStatus[]> getTaskStatuses() {
        return ResponseEntity.ok(Task.TaskStatus.values());
    }
}