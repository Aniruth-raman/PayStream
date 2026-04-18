package com.paymentIngestor.Exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path,
        List<Violation> violations
) {
    public record Violation(String field, String message) {
    }
}






