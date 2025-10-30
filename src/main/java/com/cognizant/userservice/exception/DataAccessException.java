package com.cognizant.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Generic custom exception for data access related issues.
 * This can be used for wrapping exceptions thrown by the repository layer or other service-level errors.
 * It is annotated with {@code @ResponseStatus} to automatically set the
 * HTTP status code to 500 (INTERNAL_SERVER_ERROR).
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}