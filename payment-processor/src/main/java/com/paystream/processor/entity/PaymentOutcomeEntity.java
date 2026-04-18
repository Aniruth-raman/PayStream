package com.paystream.processor.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "paymentoutcomes", indexes = {
        @Index(name = "idx_paymentoutcomes_payment_id", columnList = "payment_id"),
        @Index(name = "idx_paymentoutcomes_debit_account_id", columnList = "debit_account_id"),
        @Index(name = "idx_paymentoutcomes_credit_account_id", columnList = "credit_account_id"),
        @Index(name = "idx_paymentoutcomes_status", columnList = "status"),
        @Index(name = "idx_paymentoutcomes_processed_at", columnList = "processed_at")
})
public class PaymentOutcomeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "debit_account_id", nullable = false, length = 64)
    private String debitAccountId;

    @Column(name = "credit_account_id", nullable = false, length = 64)
    private String creditAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "processing_time_ms", nullable = false)
    private long processingTimeMs;

    public PaymentOutcomeEntity() {
    }

    public PaymentOutcomeEntity(Long id, UUID paymentId, String debitAccountId, String creditAccountId, BigDecimal amount,
                                String currency, PaymentStatus status, Instant processedAt, long processingTimeMs) {
        this.id = id;
        this.paymentId = paymentId;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.processedAt = processedAt;
        this.processingTimeMs = processingTimeMs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}

