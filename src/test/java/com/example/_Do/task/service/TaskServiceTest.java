package com.example._Do.task.service;

import com.example._Do.task.dto.TaskRequest;
import com.example._Do.task.dto.TaskResponse;
import com.example._Do.task.entity.Priority;
import com.example._Do.task.entity.Task;
import com.example._Do.task.mapper.TaskMapper;
import com.example._Do.task.repository.TaskRepository;
import com.example._Do.user.entity.User;
import com.example._Do.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for {@link TaskService}.
 * <p>
 * <strong>Goal:</strong> Test the business logic in isolation without loading the Spring Context or Database.
 * </p>
 * <p>
 * <strong>Technique:</strong> Uses Mockito to mock dependencies (Repository, Mapper, Security)
 * to verify behavior and edge cases (especially security ownership checks).
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // --- MOCKS (Dependencies) ---
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private UserRepository userRepository;

    // --- SYSTEM UNDER TEST ---
    @InjectMocks
    private TaskService taskService;

    // --- SECURITY MOCKS ---
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    // --- TEST DATA FIXTURES ---
    private User mockUser;
    private Task mockTask;
    private TaskRequest mockRequest;

    /**
     * Setup method executed before each test.
     * Initializes common objects and mocks the Security Context to simulate a logged-in user.
     */
    @BeforeEach
    void setUp() {
        // 1. Prepare a mock User
        mockUser = User.builder()
                .id(1L)
                .email("test@user.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        // 2. Prepare a mock Task belonging to the user
        mockTask = Task.builder()
                .id(100L)
                .title("Test Task")
                .user(mockUser) // Ownership link
                .priority(Priority.HIGH)
                .build();

        // 3. Prepare a mock Request DTO
        mockRequest = TaskRequest.builder()
                .title("New Task")
                .description("Description")
                .priority(Priority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        // 4. Mock the Security Context (Simulate Authentication)
        // This is crucial because TaskService relies on SecurityContextHolder to identify the user.
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Teardown method executed after each test.
     * Clears the SecurityContext to prevent state leakage between tests.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Scenario: Successfully creating a task with valid data.
     * <p>
     * Given: A valid request and an authenticated user.<br>
     * When: createTask is called.<br>
     * Then: The task should be saved to the repository and returned as a response.
     * </p>
     */
    @Test
    @DisplayName("Should create task successfully when valid request is provided")
    void createTask_WhenValidRequest_ShouldSaveAndReturnResponse() {
        // --- GIVEN ---
        // Mock Security behavior
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@user.com");

        // Mock User retrieval
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockUser));

        // Mock Mapper: DTO -> Entity
        when(taskMapper.toEntity(mockRequest)).thenReturn(mockTask);

        // Mock Repository: Save operation (return the same task)
        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);

        // Mock Mapper: Entity -> Response
        TaskResponse expectedResponse = TaskResponse.builder().id(100L).title("Test Task").build();
        when(taskMapper.toResponse(mockTask)).thenReturn(expectedResponse);

        // --- WHEN ---
        TaskResponse response = taskService.createTask(mockRequest);

        // --- THEN ---
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);

        // Verification: Ensure repository.save() was called exactly once
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    /**
     * Scenario: Retrieving a task that belongs to the authenticated user.
     * <p>
     * Given: A task exists and belongs to the current user.<br>
     * When: getTaskById is called.<br>
     * Then: The task details should be returned.
     * </p>
     */
    @Test
    @DisplayName("Should get task by ID when user owns the task")
    void getTaskById_WhenUserOwnsTask_ShouldReturnResponse() {
        // --- GIVEN ---
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@user.com");
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockUser));

        // Mock Repository finding the task
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskResponse expectedResponse = TaskResponse.builder().id(100L).build();
        when(taskMapper.toResponse(mockTask)).thenReturn(expectedResponse);

        // --- WHEN ---
        TaskResponse response = taskService.getTaskById(100L);

        // --- THEN ---
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    /**
     * Scenario: A user tries to access a task belonging to SOMEONE ELSE.
     * <p>
     * Given: Current user is User A, but Task belongs to User B.<br>
     * When: getTaskById is called.<br>
     * Then: An EntityNotFoundException should be thrown (hiding the task existence).
     * </p>
     */
    @Test
    @DisplayName("Should throw Exception when accessing someone else's task (Security Check)")
    void getTaskById_WhenNotOwned_ShouldThrowException() {
        // --- GIVEN ---
        // 1. Create another different user
        User otherUser = User.builder().id(999L).email("other@user.com").build();

        // 2. Assign the task to THAT user
        mockTask.setUser(otherUser);

        // 3. Current logged-in user is still "mockUser" (ID: 1)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@user.com");
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(mockUser));

        // 4. Repository finds the task (it exists physically in DB)
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        // --- WHEN & THEN ---
        // Verify that the service throws an exception due to ownership mismatch
        assertThatThrownBy(() -> taskService.getTaskById(100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found"); // don't reveal it exists

        // Verification: Ensure we never tried to map the response
        verify(taskMapper, never()).toResponse(any());
    }
}