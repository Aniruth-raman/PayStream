package com.paystream.processor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportSummaryResponse(
        long totalProcessed,
        long totalHeld,
        long totalRejected,
        BigDecimal totalAmountProcessed,
        Instant earliestProcessedAt,
        Instant latestProcessedAt) {
}

