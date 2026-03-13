package com.example._Do.task.dto;

import com.example._Do.task.entity.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String creatorFirstName;
    private String creatorLastName;
    private Long groupId;
    private Long assigneeId;
    private String assigneeFirstName;
    private String assigneeLastName;
    private Set<Long> assigneeIds;
    private List<String> assigneeNames;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
}