package com.example._Do.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a client exceeds the allowed number of requests
 * within a specific time window.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException(String message){
        super(message);
    }
}
