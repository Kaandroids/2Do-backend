package com.example._Do.auth;

import com.example._Do.config.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Service responsible for handling secure user logout in a stateless JWT environment.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class LogoutService implements LogoutHandler {

    private final JwtBlacklistService jwtBlacklistService;
    private final JwtService jwtService;

    /**
     * Performs the logout operation by extracting the JWT from the request,
     * calculating its remaining validity, and adding it to the global blacklist.
     *
     * @param request        The incoming HTTP request containing the Authorization header.
     * @param response       The HTTP response.
     * @param authentication The current authentication object.
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ){
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        jwt = authHeader.substring(7);

        Date expirationDate = jwtService.extractExpiration(jwt);
        Duration remainingDuration = Duration.between(Instant.now(), expirationDate.toInstant());

        jwtBlacklistService.blacklistToken(jwt, remainingDuration);

        SecurityContextHolder.clearContext();

    }
}
