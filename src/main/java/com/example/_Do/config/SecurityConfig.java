package com.example._Do.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Main configuration class for Spring Security.
 * <p>
 * This class defines the "Security Filter Chain", which acts as the gatekeeper for the application.
 * It configures which endpoints are public, how sessions are managed (stateless),
 * and integrates the custom JWT authentication filter.
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Configures the security filter chain.
     *
     * @param http The HttpSecurity object to configure.
     * @return The built SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. Disable CSRF (Cross-Site Request Forgery)
                // Since we are using JWT (Stateless auth), we don't need CSRF protection which is mainly for session-based apps.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configure URL Authorization
                .authorizeHttpRequests(auth -> auth
                        // Whitelist: Allow public access to specific endpoints without authentication
                        .requestMatchers(
                                "/api/v1/auth/**",               // Login & Register endpoints
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Blacklist: All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                // 3. Session Management
                // Set session policy to STATELESS. This ensures Spring Security does not create or use HTTP sessions.
                // Every request must carry the JWT token.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 4. Set the Authentication Provider
                .authenticationProvider(authenticationProvider)

                // 5. Add Custom Filter
                // Execute our JwtAuthenticationFilter BEFORE the standard UsernamePasswordAuthenticationFilter.
                // This allows us to intercept requests and check for the token first.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }


}