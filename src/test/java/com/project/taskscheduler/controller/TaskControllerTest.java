package com.project.taskscheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskscheduler.exception.GlobalExceptionHandler;
import com.project.taskscheduler.exception.TaskNotFoundException;
import com.project.taskscheduler.model.Task;
import com.project.taskscheduler.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TaskControllerTest {

    private final TaskService taskService = mock(TaskService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TaskController(taskService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllTasksReturnsTasks() throws Exception {
        Task task = createTask();
        UUID id = UUID.randomUUID();
        setId(task, id);

        when(taskService.getAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Task"));
    }

    @Test
    void getTaskByIdReturnsTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task task = createTask();
        setId(task, id);

        when(taskService.getTaskById(id)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"));
    }

    @Test
    void getTaskByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();

        when(taskService.getTaskById(id))
                .thenThrow(new TaskNotFoundException("Task not found with id: " + id));

        mockMvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + id));
    }

    @Test
    void createTaskReturnsCreatedTask() throws Exception {
        Task task = createTask();

        when(taskService.createTask(any(Task.class))).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Task"));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void createTaskReturnsBadRequestForInvalidBody() throws Exception {
        Task task = new Task();
        task.setDescription("Missing required fields");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updateTaskReturnsUpdatedTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task updatedTask = new Task(
                "Updated Task",
                "Updated Description",
                Task.TaskType.FIXED_DELAY,
                "2000"
        );

        when(taskService.updateTask(eq(id.toString()), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(taskService).updateTask(eq(id.toString()), any(Task.class));
    }

    @Test
    void deleteTaskReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(taskService).deleteTask(id.toString());

        mockMvc.perform(delete("/api/tasks/{id}", id))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(id.toString());
    }

    @Test
    void pauseTaskReturnsPausedTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task task = createTask();
        task.setActive(false);
        task.setStatus(Task.TaskStatus.PAUSED);

        when(taskService.pauseTask(id.toString())).thenReturn(task);

        mockMvc.perform(post("/api/tasks/{id}/pause", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.status").value("PAUSED"));
    }

    @Test
    void resumeTaskReturnsActiveTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task task = createTask();

        when(taskService.resumeTask(id.toString())).thenReturn(task);

        mockMvc.perform(post("/api/tasks/{id}/resume", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void executeTaskReturnsExecutedTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task task = createTask();

        when(taskService.executeTask(id.toString())).thenReturn(task);

        mockMvc.perform(post("/api/tasks/{id}/execute", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"));
    }

    @Test
    void getActiveTasksReturnsActiveTasks() throws Exception {
        Task task = createTask();

        when(taskService.getActiveTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTasksByStatusReturnsTasks() throws Exception {
        Task task = createTask();

        when(taskService.getTasksByStatus(Task.TaskStatus.ACTIVE)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/status/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
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