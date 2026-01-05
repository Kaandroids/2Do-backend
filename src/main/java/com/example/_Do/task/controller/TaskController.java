package com.example._Do.task.controller;

import com.example._Do.task.dto.AiTaskResponse;
import com.example._Do.task.dto.TaskRequest;
import com.example._Do.task.dto.TaskResponse;
import com.example._Do.task.service.AiTaskService;
import com.example._Do.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for managing user tasks.
 * <p>
 * Exposes endpoints for CRUD operations on Tasks.
 * Security is enforced via @PreAuthorize to ensure only authenticated users can access these resources.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for creating, retrieving, updating, and deleting tasks")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class TaskController {

    private final TaskService taskService;
    private final AiTaskService aiTaskService;

    /**
     * Creates a new task for the authenticated user.
     *
     * @param request The task creation payload containing title, description, priority, etc.
     * @return The created task with a 201 CREATED status.
     */
    @PostMapping
    @Operation(
            summary = "Create a new task",
            description = "Creates a new task record linked to the currently authenticated user."
    )
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing title)")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    /**
     * Retrieves tasks belonging to the authenticated user with pagination.
     *
     * @param pageable Pagination information (page number, size, sort).
     * @return A page of tasks owned by the user.
     */
    @GetMapping
    @Operation(
            summary = "Get my tasks (Paged)",
            description = "Retrieves a paged list of tasks belonging to the authenticated user. " +
                    "You can filter by page, size, and sort. Example: ?page=0&size=5&sort=createdAt,desc"
    )
    @ApiResponse(responseCode = "200", description = "Page of tasks retrieved successfully")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    /**
     * Retrieves a specific task by its unique ID.
     *
     * @param id The ID of the task to retrieve.
     * @return The task details if found and owned by the user.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a specific task. Throws 404 if not found or not owned by user."
    )
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found or access denied")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Updates an existing task.
     *
     * @param id      The ID of the task to update.
     * @param request The updated task details.
     * @return The updated task object.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update task",
            description = "Updates the details of an existing task."
    )
    @ApiResponse(responseCode = "200", description = "Task updated successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    /**
     * Deletes a task permanently.
     *
     * @param id The ID of the task to delete.
     * @return 204 NO CONTENT status upon successful deletion.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete task",
            description = "Permanently removes a task from the database."
    )
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ai-generate")
    public ResponseEntity<AiTaskResponse> generateTaskFromVoice(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(aiTaskService.processVoiceTask(file));
    }

}