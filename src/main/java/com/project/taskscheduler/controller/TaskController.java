package com.project.taskscheduler.controller;

import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.model.TaskType;
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
    public ResponseEntity<List<TaskDefinition>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDefinition> getTaskById(@PathVariable String id) {
        TaskDefinition taskDefinition = taskService.getTaskById(UUID.fromString(id));
        return ResponseEntity.ok(taskDefinition);
    }

    @PostMapping
    public ResponseEntity<TaskDefinition> createTask(@Valid @RequestBody TaskDefinition taskDefinition) {
        TaskDefinition createdTaskDefinition = taskService.createTask(taskDefinition);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskDefinition);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDefinition> updateTask(
            @PathVariable String id,
            @Valid @RequestBody TaskDefinition taskDefinition
    ) {
        TaskDefinition updatedTaskDefinition = taskService.updateTask(id, taskDefinition);
        return ResponseEntity.ok(updatedTaskDefinition);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/{id}/pause")
//    public ResponseEntity<TaskDefinition> pauseTask(@PathVariable String id) {
//        return ResponseEntity.ok(taskService.pauseTask(id));
//    }
//
//    @PostMapping("/{id}/resume")
//    public ResponseEntity<TaskDefinition> resumeTask(@PathVariable String id) {
//        return ResponseEntity.ok(taskService.resumeTask(id));
//    }
//
//    @PostMapping("/{id}/execute")
//    public ResponseEntity<TaskDefinition> executeTask(@PathVariable String id) {
//        return ResponseEntity.ok(taskService.executeTask(id));
//    }

    @GetMapping("/active")
    public ResponseEntity<List<TaskDefinition>> getActiveTasks() {
        return ResponseEntity.ok(taskService.getActiveTasks());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDefinition>> getTasksByStatus(@PathVariable String status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(taskService.getTasksByStatus(taskStatus));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        return ResponseEntity.ok(taskService.getTaskStatistics());
    }

    @GetMapping("/types")
    public ResponseEntity<TaskType[]> getTaskTypes() {
        return ResponseEntity.ok(TaskType.values());
    }

    @GetMapping("/statuses")
    public ResponseEntity<TaskStatus[]> getTaskStatuses() {
        return ResponseEntity.ok(TaskStatus.values());
    }
}