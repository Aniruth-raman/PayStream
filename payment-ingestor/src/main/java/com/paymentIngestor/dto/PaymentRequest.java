package com.paymentIngestor.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentRequest {

    @NotNull(message = "Payment ID must be a valid UUID")
    private UUID paymentId;

    @NotBlank(message = "Debit account ID must not be blank")
    private String debitAccountId;

    @NotBlank(message = "Credit account ID must not be blank")
    private String creditAccountId;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency must not be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO code")
    private String currency;

    @Size(max = 35, message = "Reference must not exceed 35 characters")
    private String reference; // optional

    @NotNull(message = "Timestamp must not be null")
    @PastOrPresent(message = "Timestamp must not be in the future")
    private OffsetDateTime timestamp;

    // Getters and Setters

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getDebitAccountId() {
        return debitAccountId;
    }

    public void setDebitAccountId(String debitAccountId) {
        this.debitAccountId = debitAccountId;
    }

    public String getCreditAccountId() {
        return creditAccountId;
    }

    public void setCreditAccountId(String creditAccountId) {
        this.creditAccountId = creditAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency != null ? currency.toUpperCase() : null;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}