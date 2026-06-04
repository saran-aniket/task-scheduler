package com.project.taskscheduler.controller;

import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.model.TaskType;
import com.project.taskscheduler.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DashboardController {
    private final TaskService taskService;

    public DashboardController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
//        model.addAttribute("executions", taskSchedulerService.getExecutionHistory());
//        model.addAttribute("healthCheckCount", taskSchedulerService.getHealthCheckCount());
//        model.addAttribute("cleanupCount", taskSchedulerService.getCleanupCount());
//        model.addAttribute("reportCount", taskSchedulerService.getReportCount());
        model.addAttribute("tasks", taskService.getAllTasks());
        return "dashboard";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
//        stats.put("executions", taskSchedulerService.getExecutionHistory());
//        stats.put("healthCheckCount", taskSchedulerService.getHealthCheckCount());
//        stats.put("cleanupCount", taskSchedulerService.getCleanupCount());
//        stats.put("reportCount", taskSchedulerService.getReportCount());
        stats.put("tasks", taskService.getAllTasks());
        stats.put("taskStats", taskService.getTaskStatistics());
        return stats;
    }

    @GetMapping("/api/dashboard-data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        // Task scheduler metrics
//        data.put("healthCheckCount", taskSchedulerService.getHealthCheckCount());
//        data.put("cleanupCount", taskSchedulerService.getCleanupCount());
//        data.put("reportCount", taskSchedulerService.getReportCount());
//        data.put("executions", taskSchedulerService.getExecutionHistory());

        // Task service data
        data.put("tasks", taskService.getAllTasks());
        data.put("activeTasks", taskService.getActiveTasks());
        data.put("taskStatistics", taskService.getTaskStatistics());
        data.put("taskTypes", TaskType.values());
        data.put("taskStatuses", TaskStatus.values());
        return data;
    }
}
