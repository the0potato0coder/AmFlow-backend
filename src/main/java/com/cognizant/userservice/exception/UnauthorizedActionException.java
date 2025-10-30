package com.cognizant.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an authenticated user attempts an action
 * for which they do not have sufficient permissions.
 * It is annotated with {@code @ResponseStatus} to automatically set the
 * HTTP status code to 403 (FORBIDDEN).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}