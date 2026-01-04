package com.example._Do.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Immutable response object containing the generated JWT.
 */
@Builder
@Schema(description="Response object containing the JWT access token.")
public record AuthenticationResponse (
    @Schema(
            description="JWT Access Token used for authorizing subsequent requests.",
            example="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ..."
    )
    String token
) {

}