package com.example._Do.user.exception;

/**
 * Base abstract class for all User-related exceptions.
 * Using a base class allows us to group domain-specific errors.
 */
public abstract class UserDomainException extends RuntimeException{
    protected UserDomainException(String message) {
        super(message);
    }
}
