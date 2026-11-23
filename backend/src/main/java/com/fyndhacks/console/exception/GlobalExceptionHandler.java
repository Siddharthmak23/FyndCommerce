package com.fyndhacks.console.exception;

import com.fyndhacks.console.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiExceptions.BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequest(ApiExceptions.BadRequestException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, ex, req);
    }
    @ExceptionHandler(ApiExceptions.NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ApiExceptions.NotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, ex, req);
    }
    @ExceptionHandler(ApiExceptions.ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ApiExceptions.ConflictException ex, HttpServletRequest req) {
        return response(HttpStatus.CONFLICT, ex, req);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message=ex.getBindingResult().getAllErrors().isEmpty() ? "Validation failed" :
                ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return response(HttpStatus.BAD_REQUEST, message, req);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest req) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", req);
    }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, Exception ex, HttpServletRequest req) {
        return response(status, ex.getMessage(), req);
    }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message, HttpServletRequest req) {
        ErrorResponse body=new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
