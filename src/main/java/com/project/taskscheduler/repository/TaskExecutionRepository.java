package com.project.taskscheduler.repository;

import com.project.taskscheduler.model.TaskExecution;
import com.project.taskscheduler.model.TaskExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskExecutionRepository extends JpaRepository<TaskExecution, UUID> {
    List<TaskExecution> findAllByTaskExecutionStatus(TaskExecutionStatus taskExecutionStatus);
}
