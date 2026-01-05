package com.example._Do.task.dto;

import com.example._Do.task.entity.Priority;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AiTaskResponse (
        String title,
        String description,
        Priority priority,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd['T'HH:mm:ss]")
        LocalDateTime dueDate,
        boolean isTaskDetected
){
}
