package com.paymentIngestor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentRequest {

    private static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

    @NotBlank(message = "Payment ID must be a valid UUID")
    @Pattern(regexp = UUID_PATTERN, message = "Payment ID must be a valid UUID")
    private String paymentId;

    @NotBlank(message = "Debit account ID must not be blank")
    private String debitAccountId;

    @NotBlank(message = "Credit account ID must not be blank")
    private String creditAccountId;

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency must be a 3-character ISO code")
    @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO code")
    private String currency;

    @Size(max = 35, message = "Reference must not exceed 35 characters")
    private String reference;

    @NotNull(message = "Timestamp must not be null")
    @PastOrPresent(message = "Timestamp must not be in the future")
    private OffsetDateTime timestamp;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
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