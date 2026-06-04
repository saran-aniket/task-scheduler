package com.project.taskscheduler.repository.implementation;

import com.project.taskscheduler.model.TaskDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskDefinition, UUID> {

    List<TaskDefinition> findByActiveTrue();

    List<TaskDefinition> findByStatus(TaskDefinition.TaskStatus status);
}