package com.example._Do.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;


/**
 * Immutable Data Transfer Object for user authentication.
 */
@Builder
@Schema(description="Request object containing user credentials for login.")
public record AuthenticationRequest (
    @Schema(
            description="Registered email address",
            example="john.doe@example.com"
    )
    @Email(message="Invalid email format")
    @NotBlank(message="Email is required")
    String email,

    @Schema(
            description="User's password",
            example="Secret@123"
    )
    @NotBlank(message="Password is required")
    String password
) {

}
