package com.paystream.processor.dto;

public record MetricsSummaryResponse(
        long totalProcessed,
        long totalHeld,
        long totalRejected,
        double avgProcessingTimeMs) {
}

