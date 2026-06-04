package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.repository.TaskRepository;
import com.project.taskscheduler.utility.TaskUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static com.project.taskscheduler.utility.TaskUtility.parseUuid;

@Service
public class TaskSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerService.class);

    private final TaskRepository taskRepository;
    private final TaskScheduler taskScheduler;
    private final Map<UUID, ScheduledFuture<?>> executions = new HashMap<>();

    public TaskSchedulerService(TaskRepository taskRepository, TaskScheduler taskScheduler, TaskService taskService) {
        this.taskRepository = taskRepository;
        this.taskScheduler = taskScheduler;
    }

    public TaskDefinition getTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public TaskDefinition pauseTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskStatus.PAUSED);

        return taskRepository.save(taskDefinition);
    }

    public TaskDefinition resumeTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(true);
        taskDefinition.setStatus(TaskStatus.ACTIVE);

        return taskRepository.save(taskDefinition);
    }

    public TaskDefinition executeTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setNextRun(LocalDateTime.now());

        return taskRepository.save(taskDefinition);
    }

    public TaskDefinition cancelTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskStatus.CANCELLED);
        taskDefinition.setNextRun(null);

        return taskRepository.save(taskDefinition);
    }


    public void scheduleTask(String taskDefinitionId) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(taskDefinitionId));

        if (!taskDefinition.isActive()) {
            logger.info("TaskDefinition {} is not active. Skipping schedule.", taskDefinition.getId());
            return;
        }

        if (taskDefinition.getNextRun() == null) {
            taskDefinition.setNextRun(LocalDateTime.now());
            taskDefinition = taskRepository.save(taskDefinition);
        }

//        Instant executionTime = taskDefinition.getNextRun()
//                .atZone(ZoneId.systemDefault())
//                .toInstant();


        TaskDefinition finalTaskDefinition = taskDefinition;
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {

            if (!finalTaskDefinition.isActive()) {
                logger.info("TaskDefinition {} is not active. Skipping execution.", finalTaskDefinition.getId());
                return;
            }

            logger.info("Execution has started");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            finalTaskDefinition.setLastRun(LocalDateTime.now());
            finalTaskDefinition.setNextRun(TaskUtility.getNextRunFromCron(finalTaskDefinition.getSchedule()));
            taskRepository.save(finalTaskDefinition);
        }, new CronTrigger(taskDefinition.getSchedule()));

        logger.info("TaskDefinition {} scheduled for execution at {}", taskDefinition.getId(), taskDefinition.getNextRun());
        executions.put(taskDefinition.getId(), scheduledFuture);
    }

}
