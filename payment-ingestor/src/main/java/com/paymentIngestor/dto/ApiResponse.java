package com.paymentIngestor.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private String timestamp;
    private int status;
    private String message;
    private T data;

    // ✅ Default constructor
    public ApiResponse() {
        this.timestamp = Instant.now().toString();
    }

    // ✅ All-args constructor
    public ApiResponse(int status, String message, T data) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    // Setters

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}