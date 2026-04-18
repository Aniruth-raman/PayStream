package com.paymentIngestor.Exception;

import com.paymentIngestor.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound( DuplicateAccountException ex) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(409);
        response.setMessage(ex.getMessage());
        response.setData(ex.getMessage());

        return ResponseEntity.status(409).body(response);
    }



    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(404);
        response.setMessage(ex.getMessage());
        response.setData(ex.getMessage());

        return ResponseEntity.status(404).body(response);
    }

    // 🔹 422
    @ExceptionHandler(UnprocessableException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnprocessable(UnprocessableException ex) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(422);
        response.setMessage(ex.getMessage());
        response.setData(ex.getMessage());

        return ResponseEntity.unprocessableEntity().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Collect field errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ApiResponse<Object> response = new ApiResponse<>();

        response.setStatus(400);
        response.setMessage("Validation failed");
        response.setData(errors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptionClass(Exception e) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();

        apiResponse.setStatus(500);
        apiResponse.setMessage("Internal server error");
        apiResponse.setData(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiResponse);
    }
}
