package com.example._Do.auth;

import com.example._Do.user.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name="Authentication", description="Operations related to user registration and login.")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Logout and invalidate token",
            description = "Logs out the current user by adding their JWT to the Redis blacklist. The token will be unusable until its original expiration time.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out and token invalidated."),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing Bearer token."),
            @ApiResponse(responseCode = "429", description = "Too Many Requests - Rate limit exceeded.")
    })
    @PostMapping("/logout")
    public void logout() {
        // This method is never executed. Spring Security intercepts the /api/v1/auth/logout path.
        throw new IllegalStateException("This method should be intercepted by Spring Security.");
    }

    @PostMapping("/register")
    @Operation(
            summary="Register a new user",
            description="Creates a new user account and returns a JWT token for authentication."
    )
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    @Operation(
            summary="Authenticate user",
            description="Validates credentials and returns a JWT token if successful."
    )
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
