package com.example._Do.config;

import com.example._Do.auth.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter that intercepts every incoming HTTP request to validate JWT-based authentication.
 * This filter is part of the Spring Security filter chain and ensures that:
 * 1. The request contains a valid 'Bearer' token.
 * 2. The token signature and expiration are verified.
 * 3. The token is not present in the Redis-backed blacklist (revoked tokens).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Autowired(required = false)
    private JwtBlacklistService jwtBlacklistService;

    /**
     * Core filtering logic that processes the Authorization header and orchestrates the authentication flow.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the header contains a Bearer token
        if (isValidAuthHeader(authHeader)) {
            log.trace("No Bearer token found in request headers.");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token (remove Bearer-Prefix)
        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        // Extract username (email) from token
        // Note: This might throw an exception if token is malformed, handled by Spring Security EntryPoint
        final String userEmail = jwtService.extractUsername(jwt);

        // Validate token and set authentication
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUserIfValid(request, jwt, userEmail);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Creates a standardized Authentication token with the user's authorities and request details.
     */
    private UsernamePasswordAuthenticationToken createAuthenticationToken(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

    /**
     * Verifies if the token has been revoked (e.g., after a logout) by checking the blacklist.
     */
    private boolean isBlacklisted(String jwt){
        if (jwtBlacklistService != null && jwtBlacklistService.isTokenBlacklisted(jwt)) {
            log.warn("Access denied: Token is blacklisted.");
            return true;
        }
        return false;
    }

    /**
     * Orchestrates the validation of the token and updates the SecurityContext.
     * Combines JWT claim validation with a stateful check against the blacklist.
     */
    private void authenticateUserIfValid(HttpServletRequest request, String jwt, String userEmail) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (jwtService.isTokenValid(jwt, userDetails) && !isBlacklisted(jwt)){
            UsernamePasswordAuthenticationToken authenticationToken = createAuthenticationToken(request, userDetails);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("User authenticated via JWT: {}", userEmail);
        } else {
            log.warn("Invalid JWT token for user: {}", userEmail);
        }
    }

    /**
     * Checks if the Authorization header follows the standard "Bearer <token>" format.
     */
    private boolean isValidAuthHeader(String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

}
