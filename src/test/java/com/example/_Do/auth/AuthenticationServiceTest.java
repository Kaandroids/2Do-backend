package com.example._Do.auth;


import com.example._Do.config.JwtService;
import com.example._Do.user.dto.RegisterRequest;
import com.example._Do.user.entity.Role;
import com.example._Do.user.entity.User;
import com.example._Do.user.mapper.UserMapper;
import com.example._Do.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthenticationService}
 * <p>
 * This class verifies the core security business logic, including user registration
 * and authentication flows.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    // The system under test
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("Should register a new user and return JWT token when request is valid")
    void shouldRegisterUser_WhenRequestIsValid()
    {
        // --- GIVEN
        RegisterRequest request = new RegisterRequest("Kaan", "Test", "kaan@test.com", "password123");
        String encodedPassword = "encodedPassword";
        String mockJwtToken = "mockJwtToken";
        User mockUser = User.builder()
                .firstName("Kaan")
                .lastName("Test")
                .email("kaan@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        // Simulate request -> entity
        when(userMapper.toEntity(request)).thenReturn(mockUser);

        // Simulate password encoding
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        // Simulate JWT generation
        when(jwtService.generateToken(any(User.class))).thenReturn(mockJwtToken);

        // --- ACT / WHEN
        AuthenticationResponse response = authenticationService.register(request);

        // --- ASSERT / THEN
        assertNotNull(response, "Response should not be null");
        assertEquals("mockJwtToken", response.getToken(), "Should return the generated JWT token");

        // verify that the user was actually saved to the repository
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user and return JWT token when credentials are correct")
    void shouldAuthenticateUser_WhenCredentialsAreCorrect() {
        // -- GIVEN
        AuthenticationRequest request = new AuthenticationRequest("kaan@test.com", "password123");
        String mockJwtToken = "mockJwtToken";
        User mockUser = User.builder()
                .firstName("Kaan")
                .lastName("Test")
                .email("kaan@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        // Simulate user retrieval from database
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));

        // Simulate JWT generation for the found user
        when(jwtService.generateToken(any(User.class))).thenReturn(mockJwtToken);

        // --- ACT / WHEN
        AuthenticationResponse response = authenticationService.authenticate(request);

        // --- ASSERT / THEN
        assertNotNull(response, "Response should not be null");
        assertEquals("mockJwtToken", response.getToken(), "Should return the generated JWT token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw exception when user is not found during authentication")
    void shouldThrowException_WhenUserIsNotFound() {
        // --- GIVEN
        AuthenticationRequest request = new AuthenticationRequest("Kaan", "password123");

        // Simulate case where user does not exist in database
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // --- ACT / WHEN & ASSERT / THEN
        assertThrows(Exception.class, () -> authenticationService.authenticate(request),
                "Should throw exception if user is not found");

        // Ensure JWT service is never called
        verify(jwtService, never()).generateToken(any(User.class));
    }
}
