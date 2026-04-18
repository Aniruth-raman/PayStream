package com.paystream.processor.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.paystream.processor.entity.PaymentStatus;

public record PaymentOutcome(
        UUID paymentId,
        String debitAccountId,
        String creditAccountId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant processedAt,
        long processingTimeMs) {
}

