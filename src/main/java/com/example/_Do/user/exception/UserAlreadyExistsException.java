package com.example._Do.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to register with an email that is already taken.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends UserDomainException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
