package com.example._Do.task.dto;

import com.example._Do.task.entity.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for creating or updating a task")
public class TaskRequest {

    @Schema(description = "Title of the task", example = "Complete Java Project")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Detailed description", example = "Implement JWT security and Task modules")
    private String description;

    @Schema(description = "Task priority", example = "HIGH")
    private Priority priority;

    @Schema(description = "Due date for the task", example = "2023-12-31T23:59:00")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDateTime dueDate;

    @Schema(description = "Is the task completed?", example = "false")
    private boolean completed;
}