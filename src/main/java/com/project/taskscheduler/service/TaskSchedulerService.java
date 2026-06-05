package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskExecution;
import com.project.taskscheduler.model.TaskExecutionStatus;
import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.repository.TaskDefinitionRepository;
import com.project.taskscheduler.repository.TaskExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static com.project.taskscheduler.utility.TaskUtility.getNextRunFromCron;
import static com.project.taskscheduler.utility.TaskUtility.parseUuid;

@Service
public class TaskSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerService.class);

    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final TaskScheduler taskScheduler;
    private final Map<UUID, ScheduledFuture<?>> executions = new HashMap<>();

    public TaskSchedulerService(TaskDefinitionRepository taskDefinitionRepository, TaskScheduler taskScheduler, TaskService taskService, TaskExecutionRepository taskExecutionRepository) {
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskScheduler = taskScheduler;
        this.taskExecutionRepository = taskExecutionRepository;
    }

    public TaskDefinition getTaskById(UUID id) {
        return taskDefinitionRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public TaskDefinition pauseTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskStatus.PAUSED);

        return taskDefinitionRepository.save(taskDefinition);
    }

    public TaskDefinition resumeTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(true);
        taskDefinition.setStatus(TaskStatus.ACTIVE);

        return taskDefinitionRepository.save(taskDefinition);
    }

    @Scheduled(fixedDelay = 5000)
    public void prepareTaskForExecution() {
        logger.info("Executing scheduled tasks");
        List<TaskDefinition> taskDefinitionList = taskDefinitionRepository.findAllByStatusAndNextRunBetween(TaskStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
        List<TaskExecution> existingTaskExecutions = taskExecutionRepository.findAllByTaskExecutionStatus(TaskExecutionStatus.QUEUED);
        List<TaskExecution> taskExecutionList = new ArrayList<>();
        for (TaskDefinition taskDefinition : taskDefinitionList) {
            if (existingTaskExecutions.stream().anyMatch(taskExecution -> taskExecution.getTaskDefinition().getId().equals(taskDefinition.getId()))) {
                continue;
            }
            TaskExecution taskExecution = new TaskExecution();
            taskExecution.setTaskDefinition(taskDefinition);
            taskExecution.setExecutionStartTime(LocalDateTime.now());
            taskExecution.setTaskExecutionStatus(TaskExecutionStatus.QUEUED);
            taskExecutionList.add(taskExecution);
        }
        taskExecutionList = taskExecutionRepository.saveAll(taskExecutionList);

        executeQueuedTasks(taskExecutionList);

    }

    public void executeQueuedTasks(List<TaskExecution> taskExecutionList) {
        for (TaskExecution taskExecution : taskExecutionList) {
            TaskDefinition taskDefinition = taskExecution.getTaskDefinition();
//            ScheduledFuture<?> scheduledFuture =
            taskScheduler.schedule(() -> {

                logger.info("Execution has started for Task{} at {}", taskExecution.getTaskDefinition().getName(), LocalDateTime.now());
                try {
                    taskExecution.setTaskExecutionStatus(TaskExecutionStatus.RUNNING);
                    logger.info("Execution is running for Task{}", taskExecution.getTaskDefinition().getName());
                    Thread.sleep(20000);
                    taskExecution.setTaskExecutionStatus(TaskExecutionStatus.COMPLETED);
                    logger.info("Execution has completed for Task{} at {}", taskExecution.getTaskDefinition().getName(), LocalDateTime.now());
                } catch (Exception e) {
                    taskExecution.setTaskExecutionStatus(TaskExecutionStatus.FAILED);
                    logger.error("Execution has failed for Task{}", taskExecution.getTaskDefinition().getName(), e);
//                    throw new RuntimeException(e);
                }
                taskExecutionRepository.save(taskExecution);

                //Update Next Run for Task Definition
                taskDefinition.setNextRun(getNextRunFromCron(taskDefinition.getSchedule()));
                taskDefinitionRepository.save(taskDefinition);

            }, new CronTrigger(taskDefinition.getSchedule()));

//            executions.put(taskExecution.getId(), scheduledFuture);
        }
    }

    public TaskDefinition cancelTask(String id) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(id));

        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskStatus.CANCELLED);
        taskDefinition.setNextRun(null);

        return taskDefinitionRepository.save(taskDefinition);
    }


    public void scheduleTask(String taskDefinitionId) {
        TaskDefinition taskDefinition = getTaskById(parseUuid(taskDefinitionId));

        if (!taskDefinition.isActive()) {
            logger.info("TaskDefinition {} is not active. Skipping schedule.", taskDefinition.getId());
            return;
        }

        if (taskDefinition.getNextRun() == null) {
            taskDefinition.setNextRun(LocalDateTime.now());
            taskDefinition = taskDefinitionRepository.save(taskDefinition);
        }


        logger.info("TaskDefinition {} scheduled for execution at {}", taskDefinition.getId(), taskDefinition.getNextRun());
    }

}
