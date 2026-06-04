package com.project.taskscheduler.controller;

import com.project.taskscheduler.exception.GlobalExceptionHandler;
import com.project.taskscheduler.model.TaskDefinition;
import com.project.taskscheduler.model.TaskStatus;
import com.project.taskscheduler.model.TaskType;
import com.project.taskscheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskScheduleControllerTest {

    private final TaskSchedulerService taskSchedulerService = mock(TaskSchedulerService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TaskScheduleController(taskSchedulerService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void scheduleTaskReturnsScheduledTask() throws Exception {
        UUID id = UUID.randomUUID();

        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);
        taskDefinition.setActive(true);
        taskDefinition.setStatus(TaskStatus.ACTIVE);

        mockMvc.perform(post("/api/task-schedules/{id}/schedule", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(taskSchedulerService).scheduleTask(id.toString());
    }

    @Test
    void pauseTaskReturnsPausedTask() throws Exception {
        UUID id = UUID.randomUUID();

        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);
        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskStatus.PAUSED);

        when(taskSchedulerService.pauseTask(id.toString())).thenReturn(taskDefinition);

        mockMvc.perform(post("/api/task-schedules/{id}/pause", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.status").value("PAUSED"));

        verify(taskSchedulerService).pauseTask(id.toString());
    }

    @Test
    void resumeTaskReturnsActiveTask() throws Exception {
        UUID id = UUID.randomUUID();

        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);
        taskDefinition.setActive(true);
        taskDefinition.setStatus(TaskStatus.ACTIVE);

        when(taskSchedulerService.resumeTask(id.toString())).thenReturn(taskDefinition);

        mockMvc.perform(post("/api/task-schedules/{id}/resume", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(taskSchedulerService).resumeTask(id.toString());
    }

    @Test
    void cancelTaskReturnsCancelledTask() throws Exception {
        UUID id = UUID.randomUUID();

        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);
        taskDefinition.setActive(false);
        taskDefinition.setStatus(TaskStatus.CANCELLED);
        taskDefinition.setNextRun(null);

        when(taskSchedulerService.cancelTask(id.toString())).thenReturn(taskDefinition);

        mockMvc.perform(post("/api/task-schedules/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(taskSchedulerService).cancelTask(id.toString());
    }

    @Test
    void executeTaskReturnsExecutedTask() throws Exception {
        UUID id = UUID.randomUUID();

        TaskDefinition taskDefinition = createTask();
        setId(taskDefinition, id);

        when(taskSchedulerService.executeTask(id.toString())).thenReturn(taskDefinition);

        mockMvc.perform(post("/api/task-schedules/{id}/execute", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Task"));

        verify(taskSchedulerService).executeTask(id.toString());
    }

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