package com.project.taskscheduler.controller;

import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.service.TaskSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task-schedules")
public class TaskScheduleController {

    private final TaskSchedulerService taskSchedulerService;

    public TaskScheduleController(TaskSchedulerService taskSchedulerService) {
        this.taskSchedulerService = taskSchedulerService;
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Void> scheduleTask(@PathVariable String id) {
        taskSchedulerService.scheduleTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<TaskDefinition> pauseTask(@PathVariable String id) {
        return ResponseEntity.ok(taskSchedulerService.pauseTask(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<TaskDefinition> resumeTask(@PathVariable String id) {
        return ResponseEntity.ok(taskSchedulerService.resumeTask(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TaskDefinition> cancelTask(@PathVariable String id) {
        return ResponseEntity.ok(taskSchedulerService.cancelTask(id));
    }
}