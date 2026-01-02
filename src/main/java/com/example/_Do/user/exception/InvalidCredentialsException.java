package com.example._Do.user.exception;

import com.example._Do.user.exception.UserDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends UserDomainException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
