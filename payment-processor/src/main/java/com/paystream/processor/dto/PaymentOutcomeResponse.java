package com.paystream.processor.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.paystream.processor.entity.PaymentStatus;

public record PaymentOutcomeResponse(
        UUID paymentId,
        String debitAccountId,
        String creditAccountId,
        String debitAccountName,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant processedAt,
        long processingTimeMs) {
}

