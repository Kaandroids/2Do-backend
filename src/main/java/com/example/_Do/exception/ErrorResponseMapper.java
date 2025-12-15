package com.example._Do.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Component responsible for mapping exceptions to {@link ErrorResponse} DTOs.
 * <p>
 * This separates the object construction logic from the exception handling logic,
 * adhering to the Single Responsibility Principle.
 * </p>
 */
@Component
public class ErrorResponseMapper {

    /**
     * Maps a standard exception to an ErrorResponse.
     *
     * @param exception The caught exception (used for the message).
     * @param status    The HTTP status code to be returned.
     * @param request   The HTTP request (used to extract the path).
     * @return A fully constructed ErrorResponse DTO.
     */
    public ErrorResponse mapToErrorResponse(Exception exception, HttpStatus status, HttpServletRequest request) {
        return ErrorResponse.builder()
                .statusCode(status.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }

    /**
     * Maps a custom error message to an ErrorResponse (useful for validation errors).
     *
     * @param customMessage The specific error message string.
     * @param status        The HTTP status code.
     * @param request       The HTTP request.
     * @return A fully constructed ErrorResponse DTO.
     */
    public ErrorResponse mapToErrorResponse(String customMessage, HttpStatus status, HttpServletRequest request) {
        return ErrorResponse.builder()
                .statusCode(status.value())
                .message(customMessage)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }
}