package com.paystream.processor.service;

import java.time.Instant;

import com.paystream.processor.dto.PaymentEvent;
import com.paystream.processor.dto.PaymentOutcome;
import com.paystream.processor.dto.PaymentOutcomeResponse;
import com.paystream.processor.entity.AccountEntity;
import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentOutcomeEntity toEntity(PaymentEvent event, PaymentStatus status, Instant processedAt, long processingTimeMs) {
        PaymentOutcomeEntity entity = new PaymentOutcomeEntity();
        entity.setPaymentId(event.paymentId());
        entity.setDebitAccountId(event.debitAccountId());
        entity.setCreditAccountId(event.creditAccountId());
        entity.setAmount(event.amount());
        entity.setCurrency(event.currency());
        entity.setStatus(status);
        entity.setProcessedAt(processedAt);
        entity.setProcessingTimeMs(processingTimeMs);
        return entity;
    }

    public PaymentOutcome toOutcome(PaymentOutcomeEntity entity) {
        return new PaymentOutcome(
                entity.getPaymentId(),
                entity.getDebitAccountId(),
                entity.getCreditAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getProcessedAt(),
                entity.getProcessingTimeMs());
    }

    public PaymentOutcomeResponse toResponse(PaymentOutcomeEntity entity, AccountEntity debitAccount) {
        return new PaymentOutcomeResponse(
                entity.getPaymentId(),
                entity.getDebitAccountId(),
                entity.getCreditAccountId(),
                debitAccount != null ? debitAccount.getAccountName() : null,
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getProcessedAt(),
                entity.getProcessingTimeMs());
    }
}

