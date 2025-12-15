package com.example._Do.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized exception handling component for the application.
 * <p>
 * Annotated with RestControllerAdvice, this class acts as an interceptor
 * for exceptions thrown by Controllers. It transforms specific application exceptions
 * into structured {@link ErrorResponse} objects.
 * </p>
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;

    /**
     * Handles cases where a requested resource is not found in the persistence layer.
     * <p>
     * Returns a 404 Not Found status code with a descriptive message.
     * </p>
     *
     * @param ex      The captured {@link EntityNotFoundException}.
     * @param request The HTTP request that triggered the exception.
     * @return A {@link ResponseEntity} containing the structured error details.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = errorResponseMapper.mapToErrorResponse(ex, HttpStatus.NOT_FOUND, request);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles validation failures for request bodies annotated with {@code @Valid}.
     * <p>
     * Aggregates multiple field errors into a single message string and returns
     * a 400 Bad Request status code.
     * </p>
     *
     * @param ex      The exception containing binding results and field errors.
     * @param request The HTTP request that triggered the validation failure.
     * @return A {@link ResponseEntity} containing the validation error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Collect all validation errors into a single comma-separated string
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = errorResponseMapper.mapToErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, request);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback handler for any unexpected exceptions not covered by specific handlers.
     * <p>
     * This acts as a safety net to prevent raw stack traces from being exposed to the client,
     * ensuring security and consistent error formatting. Returns HTTP 500.
     * </p>
     *
     * @param ex      The unexpected exception.
     * @param request The HTTP request.
     * @return A {@link ResponseEntity} with HTTP 500 status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ErrorResponse error = errorResponseMapper.mapToErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}