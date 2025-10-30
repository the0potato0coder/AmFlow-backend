package com.cognizant.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a user attempts to check-out but has no active check-in.
 * It is annotated with {@code @ResponseStatus} to automatically set the HTTP status code to 400 (BAD_REQUEST).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoActiveAttendanceException extends RuntimeException {

    public NoActiveAttendanceException(String message) {
        super(message);
    }
}