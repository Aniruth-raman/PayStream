package com.paymentIngestor.kafka;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentEvent(
        UUID paymentId,
        String debitAccountId,
        String creditAccountId,
        BigDecimal amount,
        String currency,
        String reference,
        OffsetDateTime timestamp
) {
}

