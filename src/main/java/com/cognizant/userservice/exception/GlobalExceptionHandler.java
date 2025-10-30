package com.cognizant.userservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that provides a centralized mechanism to handle exceptions across the application.
 * It extends {@link ResponseEntityExceptionHandler} to leverage Spring's built-in exception handling capabilities.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link UserNotFoundException} and returns a 404 NOT_FOUND response with an error message.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 404
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        logger.warn("UserNotFoundException: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link UsernameAlreadyExistsException} and returns a 409 CONFLICT response with an error message.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 409
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Object> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        logger.warn("UsernameAlreadyExistsException: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link DataAccessException} and returns a 500 INTERNAL_SERVER_ERROR response with an error message.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 500
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(DataAccessException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Failed to access data: " + ex.getMessage());
        logger.error("DataAccessException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link ActiveAttendanceExistsException} and returns a 400 BAD_REQUEST response.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 400
     */
    @ExceptionHandler(ActiveAttendanceExistsException.class)
    public ResponseEntity<Object> handleActiveAttendanceExistsException(ActiveAttendanceExistsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        logger.warn("ActiveAttendanceExistsException: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link NoActiveAttendanceException} and returns a 400 BAD_REQUEST response.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 400
     */
    @ExceptionHandler(NoActiveAttendanceException.class)
    public ResponseEntity<Object> handleNoActiveAttendanceException(NoActiveAttendanceException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        logger.warn("NoActiveAttendanceException: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link InvalidLeaveRequestException} and returns a 400 BAD_REQUEST response.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 400
     */
    @ExceptionHandler(InvalidLeaveRequestException.class)
    public ResponseEntity<Object> handleInvalidLeaveRequestException(InvalidLeaveRequestException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        logger.warn("InvalidLeaveRequestException: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles {@link UnauthorizedActionException} and returns a 403 FORBIDDEN response.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with an error message and HTTP status 403
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Object> handleUnauthorizedActionException(UnauthorizedActionException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        logger.error("UnauthorizedActionException: {}", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles generic {@link Exception} not handled by more specific handlers.
     * Returns a 500 INTERNAL_SERVER_ERROR response with a generic error message.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} with a generic error message and HTTP status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An unexpected error occurred");
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}