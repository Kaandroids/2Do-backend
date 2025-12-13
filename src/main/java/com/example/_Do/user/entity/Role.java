package com.example._Do.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example._Do.user.entity.Permission.*;

/**
 * Enumeration representing the high-level roles in the application.
 * <p>
 * Each role aggregates a set of granular Permissions.
 * This structure enables Role-Based Access Control (RBAC) combined with Authority-Based logic.
 * </p>
 */
@RequiredArgsConstructor
@Getter
public enum Role {

    /**
     * Standard user role.
     * User has no system-wide administrative permissions, only access to own data.
     */
    USER(Collections.emptySet()),

    /**
     * Administrator role.
     * Grants full access to all Admin and User operations.
     */
    ADMIN(
            Set.of(
                    ADMIN_READ,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    ADMIN_CREATE,
                    USER_READ,
                    USER_UPDATE,
                    USER_CREATE,
                    USER_DELETE
            )
    );


    private final Set<Permission> permissions;

    /**
     * Converts the permissions assigned to this role into Spring Security authorities.
     *
     * @return A list of SimpleGrantedAuthority objects including both permissions (e.g., "admin:read")
     * and the role itself (e.g., "ROLE_ADMIN").
     */
    public List<SimpleGrantedAuthority> getAuthorities() {
        // 1. Convert permissions to SimpleGrantedAuthority
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());

        // 2. Add the Role itself as an authority (Critical for hasRole() checks)

        // Spring Security expects roles to start with "ROLE_"
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}