package com.example._Do.task.repository;

import com.example._Do.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Retrieves all tasks belonging to a specific user.
     * <p>
     * This is the main method used to fetch a user's todo list.
     * </p>
     *
     * @param userId The ID of the user.
     * @return List of tasks owned by the user.
     */
    Page<Task> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Retrieves tasks for a specific user filtered by completion status.
     * <p>
     * Example: Get all "Pending" tasks for User X.
     * </p>
     *
     * @param userId    The ID of the user.
     * @param completed The completion status (true for done, false for pending).
     * @return List of matching tasks.
     */
    List<Task> findAllByUserIdAndCompleted(Long userId, boolean completed);
}
