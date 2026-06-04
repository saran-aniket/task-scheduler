package com.project.taskscheduler.utility;

import com.project.taskscheduler.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskUtility {
    public static final Logger logger = LoggerFactory.getLogger(TaskUtility.class);
    public static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new TaskNotFoundException("Invalid task id: " + id);
        }
    }

    public static boolean validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return false;
        }
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static LocalDateTime getNextRunFromCron(String cronExpression) {
        if (!validateCronExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid CRON expression: " + cronExpression);
        }
        logger.info("Calculating next run for CRON expression: {}", cronExpression);
        CronExpression expression = CronExpression.parse(cronExpression);
        return expression.next(LocalDateTime.now());
    }
}
