package com.example._Do.user.service;

import com.example._Do.user.dto.UserCreateRequest;
import com.example._Do.user.dto.UserResponse;
import com.example._Do.user.entity.User;
import com.example._Do.user.exception.UserAlreadyExistsException;
import com.example._Do.user.mapper.UserMapper;
import com.example._Do.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for User Management operations.
 * <p>
 * This service handles administrative tasks such as listing all users and
 * manually creating users (e.g., by an Admin) without generating a login token.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users. Typically used by Admin Dashboards.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.info("Retrieving all users list");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new user manually (Admin feature).
     * <p>
     * It creates the record in the database.
     * </p>
     *
     * @param request The user details.
     * @return The created UserResponse DTO.
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Admin creating a new user with email: {}", request.getEmail());

        // 1. Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists.");
        }

        // 2. Build Entity
        User user = userMapper.toEntity(request);

        // CRITICAL: Encode password manually
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. Save to DB
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // 4. Return DTO (No Token)
        return userMapper.toResponse(savedUser);
    }
}