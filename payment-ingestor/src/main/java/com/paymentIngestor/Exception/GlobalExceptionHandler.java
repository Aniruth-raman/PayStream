package com.paymentIngestor.Exception;

import com.paymentIngestor.Exception.AccountNotFoundException;
import com.paymentIngestor.Exception.AccountSuspendedException;
import com.paymentIngestor.Exception.ApiErrorResponse;
import com.paymentIngestor.Exception.DuplicatePaymentException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                             HttpServletRequest request) {
        List<ApiErrorResponse.Violation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiErrorResponse.Violation(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                      HttpServletRequest request) {
        List<ApiErrorResponse.Violation> violations = exception.getConstraintViolations().stream()
                .map(violation -> new ApiErrorResponse.Violation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(AccountNotFoundException exception,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Account not found", request.getRequestURI(),
                List.of(new ApiErrorResponse.Violation("accountId", exception.getMessage())));
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicatePayment(DuplicatePaymentException exception,
                                                                   HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Duplicate paymentId", request.getRequestURI(),
                List.of(new ApiErrorResponse.Violation("paymentId", exception.getMessage())));
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ApiErrorResponse> handleSuspended(AccountSuspendedException exception,
                                                            HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Account suspended", request.getRequestURI(),
                List.of(new ApiErrorResponse.Violation("accountId", exception.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI(),
                List.of(new ApiErrorResponse.Violation(null, exception.getMessage())));
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status,
                                                    String error,
                                                    String path,
                                                    List<ApiErrorResponse.Violation> violations) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                path,
                violations
        ));
    }
}
