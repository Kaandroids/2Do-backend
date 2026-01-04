package com.example._Do.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request object for registering a new user")
public record RegisterRequest (

    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = "First name is required")
    String firstName,

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    String lastName,

    @Schema(
            description = "User's email address (must be unique)",
            example = "john.doe@example.com"
    )
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email,

    @Schema(
            description = "User's raw password",
            example = "Secret@123"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password

) {

}