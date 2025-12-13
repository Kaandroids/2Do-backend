package com.example._Do.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration defining granular permissions (authorities) for the system resources.
 * <p>
 * Naming Convention: RESOURCE_ACTION (e.g., ADMIN_READ).
 * Value Convention: "resource:action" (used by Spring Security).
 * </p>
 */
@RequiredArgsConstructor
@Getter
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete");

    /**
     * The string representation of the permission used by Spring Security authorities.
     */
    private final String permission;
}