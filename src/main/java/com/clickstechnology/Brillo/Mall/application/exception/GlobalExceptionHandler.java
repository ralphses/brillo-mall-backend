package com.clickstechnology.Brillo.Mall.application.exception;

import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handle validation errors */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBuilder.error(errors, "Validation failed", HttpStatus.BAD_REQUEST.value()));
    }

    /** Handle entity not found (custom not found case) */
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ResponseWrapper<String>> handleEntityNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseBuilder.error(null, ex.getMessage() != null ? ex.getMessage() : "Resource not found", HttpStatus.NOT_FOUND.value()));
    }

    /** Handle manual ResponseStatusException (e.g., thrown explicitly in services) */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResponseWrapper<String>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ResponseBuilder.error("", ex.getReason(), ex.getStatusCode().value()));
    }

    /** Handle bad JSON or missing request body */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<String>> handleBadRequest(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBuilder.error("", "Malformed JSON request", HttpStatus.BAD_REQUEST.value()));
    }

    /** Handle business */
    @ExceptionHandler({BusinessException.class, BadCredentialsException.class})
    public ResponseEntity<ResponseWrapper<String>> handleBusiness(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBuilder.error("", ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    /** Handle database constraint violations */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseWrapper<String>> handleDatabaseError(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseBuilder.error("", "Data constraint violation", HttpStatus.CONFLICT.value()));
    }

    /** Handle unauthorized or forbidden access */
    @ExceptionHandler({AccessDeniedException.class, UnauthorizedUserException.class})
    public ResponseEntity<ResponseWrapper<String>> handleAccessDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseBuilder.error("", "Access denied", HttpStatus.FORBIDDEN.value()));
    }

    /** Handle unsupported HTTP methods */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseWrapper<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ResponseBuilder.error("", "HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    /** Handle missing multipart file */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ResponseWrapper<String>> handleMissingFile(MissingServletRequestPartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBuilder.error("", ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    /** Catch all other exceptions */
    @ExceptionHandler({Exception.class, ApplicationException.class})
    public ResponseEntity<ResponseWrapper<String>> handleGeneralException(Exception ex) {
       log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseBuilder.error("", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}