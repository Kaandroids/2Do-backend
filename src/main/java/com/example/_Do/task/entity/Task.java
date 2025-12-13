package com.example._Do.task.entity;

import com.example._Do.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity class representing a Task in the database.
 * <p>
 * This class maps to the 'tasks' table and includes relationships to the {@link User} entity,
 * along with automatic timestamp auditing.
 * </p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Task title is required")
    private String title;

    @Column(columnDefinition = "TEXT") // Allows storing long descriptions
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private boolean completed;

    // --- RELATIONSHIPS ---

    /**
     * The user who owns this task.
     * <p>
     * FetchType.LAZY is used for performance; the user data is loaded only when explicitly requested.
     * ToString.Exclude prevents circular references/infinite loops during debugging/logging.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- AUDIT FIELDS ---

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Automatically sets the creation timestamp and default values before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (priority == null) {
            priority = Priority.MEDIUM; // Default priority
        }
    }

    /**
     * Automatically updates the timestamp before any update operation.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", completed=" + completed +
                ", dueDate=" + dueDate +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}
