package com.project.taskscheduler.repository;

import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDefinitionRepository extends JpaRepository<TaskDefinition, UUID> {

    List<TaskDefinition> findByActiveTrue();

    List<TaskDefinition> findByStatus(TaskStatus status);

    List<TaskDefinition> findAllByStatusAndNextRunBetween(TaskStatus status, LocalDateTime start, LocalDateTime end);
}