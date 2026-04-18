package com.paystream.processor.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentEvent(
        @NotNull UUID paymentId,
        @NotBlank String debitAccountId,
        @NotBlank String creditAccountId,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        String reference,
        @NotNull Instant timestamp) {
}

