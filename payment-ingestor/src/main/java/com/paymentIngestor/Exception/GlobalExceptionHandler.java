package com.paymentIngestor.Exception;

import com.paymentIngestor.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

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
