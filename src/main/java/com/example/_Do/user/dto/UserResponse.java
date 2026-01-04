package com.example._Do.user.dto;

import com.example._Do.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Response object containing safe user details (no sensitive data like passwords)")
public record UserResponse (

    @Schema(description = "User's first name", example = "John")
    String firstName,

    @Schema(description = "User's last name", example = "Doe")
    String lastName,

    @Schema(description = "User's email address", example = "john.doe@example.com")
    String email,

    @Schema(description = "Assigned system role", example = "ROLE_USER")
    Role role
){
}