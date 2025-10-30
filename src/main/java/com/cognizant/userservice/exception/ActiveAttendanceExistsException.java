package com.cognizant.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ActiveAttendanceExistsException extends RuntimeException {

    public ActiveAttendanceExistsException(String message) {
        super(message);
    }
}