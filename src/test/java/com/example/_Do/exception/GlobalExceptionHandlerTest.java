package com.example._Do.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 * <p>
 * This test class verifies that the exception handler correctly intercepts
 * exceptions and maps them to the appropriate {@link ErrorResponse} structure
 * with the correct HTTP status codes.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    /**
     * The subject under test.
     * Mockito will automatically inject any @Mock fields into this instance if needed.
     */
    private GlobalExceptionHandler globalExceptionHandler;

    /**
     * Mocked HttpServletRequest to simulate the web request context.
     * Used to verify that the request path is correctly captured in the error response.
     */
    @Mock
    private HttpServletRequest request;

    /**
     * Prepares the test environment before each test method execution.
     * <p>
     * Manually initializes the {@link GlobalExceptionHandler} to ensure a fresh instance
     * is available for every test case. Direct instantiation is preferred here as the
     * handler is stateless and does not require complex dependency injection.
     * </p>
     */
    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return 404 Not Found when EntityNotFoundException is thrown")
    void shouldReturn404_WhenEntityNotFound() {
        // Arrange
        String exceptionMessage = "Task not found with id: 1";
        String requestUri = "/api/v1/tasks/1";
        EntityNotFoundException exception = new EntityNotFoundException(exceptionMessage);

        when(request.getRequestURI()).thenReturn(requestUri);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEntityNotFound(exception, request);

        // Assert
        assertNotNull(response, "Response entity should not be null");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "HTTP Status should be 404");

        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(HttpStatus.NOT_FOUND.value(), body.getStatusCode());
        assertEquals(exceptionMessage, body.getMessage());
        assertEquals(requestUri, body.getPath());
        assertNotNull(body.getTimestamp(), "Timestamp should be generated");
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when a generic Exception is thrown")
    void shouldReturn500_WhenGenericExceptionOccurs() {
        // --- GIVEN ---
        String exceptionMessage = "Database connection failed";
        String requestUri = "/api/v1/tasks";
        RuntimeException exception = new RuntimeException(exceptionMessage);

        // --- WHEN & THEN ---
        when(request.getRequestURI()).thenReturn(requestUri);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(exception, request);

        // Assert
        assertNotNull(response, "Response entity should not be null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP Status should be 500");

        ErrorResponse body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.getStatusCode());
        assertTrue(body.getMessage().contains(exceptionMessage), "Message should contain the original error details");
        assertEquals(requestUri, body.getPath());
        assertNotNull(body.getTimestamp(), "Timestamp should be generated");
    }
}