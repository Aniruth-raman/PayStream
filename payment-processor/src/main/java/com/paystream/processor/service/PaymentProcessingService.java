package com.paystream.processor.service;

import java.time.Instant;
import java.util.Objects;

import com.paystream.processor.config.AppKafkaProperties;
import com.paystream.processor.dto.PaymentEvent;
import com.paystream.processor.dto.PaymentOutcome;
import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import com.paystream.processor.repository.PaymentOutcomeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentOutcomeRepository paymentOutcomeRepository;
    private final KafkaTemplate<String, PaymentOutcome> kafkaTemplate;
    private final MetricsService metricsService;
    private final PaymentMapper paymentMapper;
    private final AppKafkaProperties appKafkaProperties;

    public PaymentProcessingService(PaymentOutcomeRepository paymentOutcomeRepository,
                                    KafkaTemplate<String, PaymentOutcome> kafkaTemplate,
                                    MetricsService metricsService,
                                    PaymentMapper paymentMapper,
                                    AppKafkaProperties appKafkaProperties) {
        this.paymentOutcomeRepository = paymentOutcomeRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metricsService = metricsService;
        this.paymentMapper = paymentMapper;
        this.appKafkaProperties = appKafkaProperties;
    }

    @Transactional
    public PaymentOutcome process(PaymentEvent event) {
        Objects.requireNonNull(event, "payment event must not be null");
        validate(event);

        long startNanos = System.nanoTime();
        PaymentStatus status = determineStatus(event);
        Instant processedAt = Instant.now();
        long processingTimeMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);

        log.info("Processing paymentId={} debitAccountId={} creditAccountId={} amount={} currency={} status={}",
                event.paymentId(), event.debitAccountId(), event.creditAccountId(), event.amount(), event.currency(), status);

        PaymentOutcomeEntity saved = paymentOutcomeRepository.save(
                paymentMapper.toEntity(event, status, processedAt, processingTimeMs));
        PaymentOutcome outcome = paymentMapper.toOutcome(saved);

        Runnable publishAndCount = () -> {
            // The model includes REJECTED outcomes as well; we publish all statuses so the processor topic remains a complete audit stream.
            log.info("Publishing processed outcome for paymentId={} to topic={}", outcome.paymentId(), appKafkaProperties.topics().paymentsProcessed());
            kafkaTemplate.send(appKafkaProperties.topics().paymentsProcessed(), saved.getDebitAccountId(), outcome).join();
            metricsService.increment(status, processingTimeMs);
            log.info("Completed paymentId={} persistedId={} processingTimeMs={}", outcome.paymentId(), saved.getId(), processingTimeMs);
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAndCount.run();
                }
            });
        } else {
            publishAndCount.run();
        }

        return outcome;
    }

    private PaymentStatus determineStatus(PaymentEvent event) {
        if (event.debitAccountId().equals(event.creditAccountId())) {
            return PaymentStatus.REJECTED;
        }
        if (event.amount().compareTo(new java.math.BigDecimal("250000")) > 0) {
            return PaymentStatus.HELD;
        }
        return PaymentStatus.PROCESSED;
    }

    private void validate(PaymentEvent event) {
        if (!StringUtils.hasText(event.debitAccountId())) {
            throw new IllegalArgumentException("debitAccountId must not be blank");
        }
        if (!StringUtils.hasText(event.creditAccountId())) {
            throw new IllegalArgumentException("creditAccountId must not be blank");
        }
        if (event.amount() == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (event.currency() == null || event.currency().isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
    }
}

