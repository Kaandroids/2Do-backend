package com.example._Do.auth.service;

import com.example._Do.auth.dto.AuthenticationRequest;
import com.example._Do.auth.dto.AuthenticationResponse;
import com.example._Do.user.dto.RegisterRequest;
import com.example._Do.user.entity.Role;
import com.example._Do.user.entity.User;
import com.example._Do.user.exception.InvalidCredentialsException;
import com.example._Do.user.exception.UserAlreadyExistsException;
import com.example._Do.user.mapper.UserMapper;
import com.example._Do.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling user authentication and registration logic.
 * <p>
 * This service interacts with the database to save users and generates
 * JWT tokens upon successful authentication.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    /**
     * Creates a new user account and returns a JWT token.
     *
     * @param request The registration request containing user details.
     * @return AuthenticationResponse containing the generated JWT token.
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {

        log.info("Attempting to register new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email {} is already in use", request.email());
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists.");
        }

        User user = userMapper.toEntity(request);
        // Encode the password (mapper copied the raw one)
        user.setPassword(passwordEncoder.encode(request.password()));
        // Force ROLE_USER for public registration (Safety measure)
        user.setRole(Role.USER);
        userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request The authentication request containing email and password.
     * @return AuthenticationResponse containing the generated JWT token.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        log.info("Authenticating user: {}", request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new InvalidCredentialsException("Invalid username or password.")
        );

        String jwtToken = jwtService.generateToken(user);
        log.info("User authenticated successfully: {}", user.getEmail());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
