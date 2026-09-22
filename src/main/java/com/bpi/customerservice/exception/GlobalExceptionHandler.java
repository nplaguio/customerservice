package com.bpi.customerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";
    private static final String CODE_KEY = "code";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_KEY, ex.getReason());
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    // 404
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomerNotFound(CustomerNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(CODE_KEY, ex.getErrorCode().getCode());
        errorResponse.put(MESSAGE_KEY, ex.getErrorCode().getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_KEY, "The requested API endpoint does not exist.");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_KEY, "Internal Server Error: Unable to process request or connect to database.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 400
    @ExceptionHandler(InvalidAccountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAccount(InvalidAccountException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(CODE_KEY, ex.getErrorCode().getCode());
        errorResponse.put(MESSAGE_KEY, ex.getErrorCode().getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 503
    @ExceptionHandler(BranchServiceException.class)
    public ResponseEntity<Map<String, String>> handleBranchServiceDown(BranchServiceException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(CODE_KEY, ex.getErrorCode().getCode());
        errorResponse.put(MESSAGE_KEY, ex.getErrorCode().getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}