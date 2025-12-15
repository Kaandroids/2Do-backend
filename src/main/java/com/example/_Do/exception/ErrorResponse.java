package com.example._Do.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standardized error response object returned by the API when an exception occurs")
public class ErrorResponse {

    @Schema(
            description = "HTTP status code of the error",
            example = "404"
    )
    private int statusCode;

    @Schema(
            description = "Detailed error message explaining what went wrong",
            example = "Task not found with id: 10"
    )
    private String message;

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2024-12-14T10:15:30"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "The API path where the request was initiated",
            example = "/api/v1/tasks/10"
    )
    private String path;
}