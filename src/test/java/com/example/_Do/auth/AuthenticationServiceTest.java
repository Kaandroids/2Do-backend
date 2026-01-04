package com.example._Do.auth;

import com.example._Do.auth.dto.AuthenticationRequest;
import com.example._Do.auth.dto.AuthenticationResponse;
import com.example._Do.auth.service.AuthenticationService;
import com.example._Do.config.JwtService;
import com.example._Do.user.dto.RegisterRequest;
import com.example._Do.user.entity.Role;
import com.example._Do.user.entity.User;
import com.example._Do.user.exception.InvalidCredentialsException;
import com.example._Do.user.exception.UserAlreadyExistsException;
import com.example._Do.user.mapper.UserMapper;
import com.example._Do.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserMapper userMapper;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Success: Should hash password and force ROLE_USER")
        void register_ShouldSucceed_WhenRequestIsValid() {
            // GIVEN
            RegisterRequest registerRequest = createSampleRegisterRequest();
            User mappedUser = new User();

            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            when(userMapper.toEntity(registerRequest)).thenReturn(mappedUser);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashedPassword");
            when(jwtService.generateToken(any())).thenReturn("validToken");

            // ACT
            AuthenticationResponse authenticationResponse = authenticationService.register(registerRequest);

            // ASSERT & VERIFY
            assertNotNull(authenticationResponse);
            assertEquals("validToken", authenticationResponse.token());

            verify(userRepository).save(userArgumentCaptor.capture());
            User savedUser = userArgumentCaptor.getValue();

            assertAll("Final User Object",
                    () -> assertEquals("hashedPassword", savedUser.getPassword(), "Password should be hashed"),
                    () -> assertEquals(Role.USER, savedUser.getRole(), "Role must be USER regardless of request"),
                    () -> verify(passwordEncoder, times(1)).encode(anyString())
            );

        }

        @Test
        @DisplayName("Fail: Should throw UserAlreadyExistsException and stop execution")
        void register_ShouldFail_WhenUserAlreadyExists() {
            // GIVEN
            RegisterRequest registerRequest = createSampleRegisterRequest();
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

            // ACT
            assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(registerRequest));

            // Verify Fail
            verifyNoInteractions(userMapper, passwordEncoder, jwtService);
            verify(userRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Success: Should return token for valid credentials")
        void authenticate_ShouldSucceed_WhenCredentialsAreValid() {
            // GIVEN
            AuthenticationRequest authenticationRequest = createSampleAuthenticationRequest();
            User mockUser = User.builder()
                    .email(authenticationRequest.email())
                    .password(authenticationRequest.password())
                    .build();

            when(userRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.of(mockUser));
            when(jwtService.generateToken(mockUser)).thenReturn("validToken");

            // ACT
            AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);

            // ASSERT & VERIFY
            assertNotNull(authenticationResponse);
            assertEquals("validToken", authenticationResponse.token());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        }

        @Test
        @DisplayName("Fail: Should return InvalidCredentialsException for invalid credentials")
        void authenticate_ShouldFail_WhenCredentialsAreInvalid() {
            // GIVEN
            AuthenticationRequest authenticationRequest = createSampleAuthenticationRequest();
            doThrow(new BadCredentialsException("Failed")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

            // ACT & ASSERT
            assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(authenticationRequest));
            verifyNoInteractions(jwtService, userRepository);
        }

    }

    // --- Helper Methods ---
    private RegisterRequest createSampleRegisterRequest()
    {
        return new RegisterRequest("Kaan", "Test", "kaan@test.com", "password123");
    }

    private AuthenticationRequest createSampleAuthenticationRequest()
    {
        return new AuthenticationRequest("Kaan", "password123");
    }

}
