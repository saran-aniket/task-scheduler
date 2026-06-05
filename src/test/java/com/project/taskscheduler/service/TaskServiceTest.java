package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.model.TaskType;
import com.project.taskscheduler.repository.TaskDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskDefinitionRepository taskDefinitionRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasksReturnsTasks() {
        TaskDefinition taskDefinition = createTask();

        when(taskDefinitionRepository.findAll()).thenReturn(List.of(taskDefinition));

        List<TaskDefinition> taskDefinitions = taskService.getAllTasks();

        assertEquals(1, taskDefinitions.size());
        assertEquals("Test Task", taskDefinitions.getFirst().getName());
        verify(taskDefinitionRepository).findAll();
    }

    @Test
    void getTaskByIdReturnsTaskWhenFound() {
        UUID id = UUID.randomUUID();
        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);

        when(taskDefinitionRepository.findById(id)).thenReturn(Optional.of(taskDefinition));

        TaskDefinition result = taskService.getTaskById(id);

        assertEquals(id, result.getId());
        assertEquals("Test Task", result.getName());
    }

    @Test
    void getTaskByIdThrowsWhenTaskNotFound() {
        UUID id = UUID.randomUUID();

        when(taskDefinitionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(id));
    }

    @Test
    void createTaskSavesTask() {
        TaskDefinition taskDefinition = createTask();

        when(taskDefinitionRepository.save(taskDefinition)).thenReturn(taskDefinition);

        TaskDefinition result = taskService.createTask(taskDefinition);

        assertEquals(TaskStatus.ACTIVE, result.getStatus());
        assertTrue(result.isActive());
        verify(taskDefinitionRepository).save(taskDefinition);
    }

    @Test
    void updateTaskUpdatesExistingTask() {
        UUID id = UUID.randomUUID();

        TaskDefinition existingTaskDefinition = createTask();
        setId(existingTaskDefinition, id);

        TaskDefinition updatedTaskDefinition = new TaskDefinition(
                "Updated Task",
                "Updated Description",
                TaskType.FIXED_DELAY,
                "2000"
        );

        when(taskDefinitionRepository.findById(id)).thenReturn(Optional.of(existingTaskDefinition));
        when(taskDefinitionRepository.save(existingTaskDefinition)).thenReturn(existingTaskDefinition);

        TaskDefinition result = taskService.updateTask(id.toString(), updatedTaskDefinition);

        assertEquals("Updated Task", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskType.FIXED_DELAY, result.getType());
        assertEquals("2000", result.getSchedule());
        verify(taskDefinitionRepository).save(existingTaskDefinition);
    }

    @Test
    void deleteTaskDeletesExistingTask() {
        UUID id = UUID.randomUUID();

        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);

        when(taskDefinitionRepository.findById(id)).thenReturn(Optional.of(taskDefinition));

        taskService.deleteTask(id.toString());

        verify(taskDefinitionRepository).delete(taskDefinition);
    }

//    @Test
//    void pauseTaskUpdatesStatusToPaused() {
//        UUID id = UUID.randomUUID();
//
//        TaskDefinition taskDefinition = createTask();
//        setId(taskDefinition, id);
//
//        when(taskRepository.findById(id)).thenReturn(Optional.of(taskDefinition));
//        when(taskRepository.save(taskDefinition)).thenReturn(taskDefinition);
//
//        TaskDefinition result = taskService.pauseTask(id.toString());
//
//        assertFalse(result.isActive());
//        assertEquals(TaskDefinition.TaskStatus.PAUSED, result.getStatus());
//        verify(taskRepository).save(taskDefinition);
//    }

//    @Test
//    void resumeTaskUpdatesStatusToActive() {
//        UUID id = UUID.randomUUID();
//
//        TaskDefinition taskDefinition = createTask();
//        taskDefinition.setActive(false);
//        taskDefinition.setStatus(TaskDefinition.TaskStatus.PAUSED);
//        setId(taskDefinition, id);
//
//        when(taskRepository.findById(id)).thenReturn(Optional.of(taskDefinition));
//        when(taskRepository.save(taskDefinition)).thenReturn(taskDefinition);
//
//        TaskDefinition result = taskService.resumeTask(id.toString());
//
//        assertTrue(result.isActive());
//        assertEquals(TaskDefinition.TaskStatus.ACTIVE, result.getStatus());
//        assertNotNull(result.getNextRun());
//        verify(taskRepository).save(taskDefinition);
//    }

//    @Test
//    void executeTaskUpdatesLastRunAndNextRun() {
//        UUID id = UUID.randomUUID();
//
//        TaskDefinition taskDefinition = createTask();
//        setId(taskDefinition, id);
//
//        when(taskRepository.findById(id)).thenReturn(Optional.of(taskDefinition));
//        when(taskRepository.save(taskDefinition)).thenReturn(taskDefinition);
//
//        TaskDefinition result = taskService.executeTask(id.toString());
//
//        assertNotNull(result.getLastRun());
//        assertNotNull(result.getNextRun());
//        verify(taskRepository).save(taskDefinition);
//    }

    private TaskDefinition createTask() {
        return new TaskDefinition(
                "Test Task",
                "Test Description",
                TaskType.FIXED_RATE,
                "1000"
        );
    }

    private void setId(TaskDefinition taskDefinition, UUID id) {
        try {
            Field field = taskDefinition.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(taskDefinition, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}