package com.project.taskscheduler.service;

import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.Task;
import com.project.taskscheduler.repository.implementation.TaskRepository;
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
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasksReturnsTasks() {
        Task task = createTask();

        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.getFirst().getName());
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskByIdReturnsTaskWhenFound() {
        UUID id = UUID.randomUUID();
        Task task = createTask();
        setId(task, id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(id);

        assertEquals(id, result.getId());
        assertEquals("Test Task", result.getName());
    }

    @Test
    void getTaskByIdThrowsWhenTaskNotFound() {
        UUID id = UUID.randomUUID();

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(id));
    }

    @Test
    void createTaskSavesTask() {
        Task task = createTask();

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertEquals(Task.TaskStatus.ACTIVE, result.getStatus());
        assertTrue(result.isActive());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskUpdatesExistingTask() {
        UUID id = UUID.randomUUID();

        Task existingTask = createTask();
        setId(existingTask, id);

        Task updatedTask = new Task(
                "Updated Task",
                "Updated Description",
                Task.TaskType.FIXED_DELAY,
                "2000"
        );

        when(taskRepository.findById(id)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(id.toString(), updatedTask);

        assertEquals("Updated Task", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(Task.TaskType.FIXED_DELAY, result.getType());
        assertEquals("2000", result.getSchedule());
        verify(taskRepository).save(existingTask);
    }

    @Test
    void deleteTaskDeletesExistingTask() {
        UUID id = UUID.randomUUID();

        Task task = createTask();
        setId(task, id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        taskService.deleteTask(id.toString());

        verify(taskRepository).delete(task);
    }

    @Test
    void pauseTaskUpdatesStatusToPaused() {
        UUID id = UUID.randomUUID();

        Task task = createTask();
        setId(task, id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.pauseTask(id.toString());

        assertFalse(result.isActive());
        assertEquals(Task.TaskStatus.PAUSED, result.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void resumeTaskUpdatesStatusToActive() {
        UUID id = UUID.randomUUID();

        Task task = createTask();
        task.setActive(false);
        task.setStatus(Task.TaskStatus.PAUSED);
        setId(task, id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.resumeTask(id.toString());

        assertTrue(result.isActive());
        assertEquals(Task.TaskStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getNextRun());
        verify(taskRepository).save(task);
    }

    @Test
    void executeTaskUpdatesLastRunAndNextRun() {
        UUID id = UUID.randomUUID();

        Task task = createTask();
        setId(task, id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.executeTask(id.toString());

        assertNotNull(result.getLastRun());
        assertNotNull(result.getNextRun());
        verify(taskRepository).save(task);
    }

    private Task createTask() {
        return new Task(
                "Test Task",
                "Test Description",
                Task.TaskType.FIXED_RATE,
                "1000"
        );
    }

    private void setId(Task task, UUID id) {
        try {
            Field field = task.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(task, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}