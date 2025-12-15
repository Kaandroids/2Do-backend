package com.example._Do.task.service;

import com.example._Do.task.dto.TaskRequest;
import com.example._Do.task.dto.TaskResponse;
import com.example._Do.task.entity.Task;
import com.example._Do.task.mapper.TaskMapper;
import com.example._Do.task.repository.TaskRepository;
import com.example._Do.user.entity.User;
import com.example._Do.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing Task operations.
 * <p>
 * Implements business logic for creating, retrieving, updating, and deleting tasks.
 * Crucially, it enforces ownership security: A user can only manage their own tasks.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;

    /**
     * Creates a new task for the currently authenticated user.
     *
     * @param taskRequest The task details.
     * @return The created task response.
     */
    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {
        User currentUser = getCurrentUser();
        log.info("Creating task for user: {}", currentUser);

        // 1. DTO -> Entity
        Task task = taskMapper.toEntity(taskRequest);

        // 2. Set the owner (CRITICAL)
        task.setUser(currentUser);

        // 3. Save and return
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }


    /**
     * Retrieves all tasks belonging to the current user.
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        User currentUser = getCurrentUser();
        log.info("Retrieving all tasks for user: {}", currentUser);

        Page<Task> taskPage = taskRepository.findAllByUserId(currentUser.getId(), pageable);

        return taskPage.map(taskMapper::toResponse);
    }

    /**
     * Retrieves a specific task by ID, strictly ensuring ownership.
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        return taskMapper.toResponse(task);
    }

    /**
     * Updates an existing task.
     * Uses MapStruct to merge changes onto the existing entity.
     */
    @Transactional
    public TaskResponse updateTask(Long taskId,TaskRequest taskRequest) {
        Task task = getTaskOrThrow(taskId);

        log.info("Updating task ID: {}", task);

        taskMapper.updateEntityFromRequest(taskRequest,task);

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    /**
     * Deletes a task by ID.
     */
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        log.info("Deleting task ID: {}", task);
        taskRepository.delete(task);
    }

    // --- HELPER METHODS ---

    /**
     * Helper method to get the currently authenticated User from the SecurityContext.
     * This avoids passing userId as a parameter in every method.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Helper method to find a task AND check ownership.
     * If task doesn't exist OR belongs to another user, it throws exception.
     */
    private Task getTaskOrThrow(Long taskId) {
        User user = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        // SECURITY CHECK: is this my task ?
        if (!task.getUser().getId().equals(user.getId())) {
            log.warn("User {} tried to access Task {} which belongs to User {}",
                    user.getId(), taskId, task.getUser().getId());
            throw new EntityNotFoundException("Task not found"); // Hide existence for security
        }

        return task;
    }

}
