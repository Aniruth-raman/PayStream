package com.paystream.processor.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.paystream.processor.config.AppKafkaProperties;
import com.paystream.processor.dto.PaymentEvent;
import com.paystream.processor.dto.PaymentOutcome;
import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import com.paystream.processor.repository.PaymentOutcomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

    @Mock
    private PaymentOutcomeRepository paymentOutcomeRepository;

    @Mock
    private KafkaTemplate<String, PaymentOutcome> kafkaTemplate;

    private MetricsService metricsService;
    private PaymentMapper paymentMapper;
    private PaymentProcessingService paymentProcessingService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService();
        paymentMapper = new PaymentMapper();
        paymentProcessingService = new PaymentProcessingService(
                paymentOutcomeRepository,
                kafkaTemplate,
                metricsService,
                paymentMapper,
                new AppKafkaProperties(new AppKafkaProperties.Topics("payments.submitted", "payments.processed")));

        when(kafkaTemplate.send(any(String.class), any(String.class), any(PaymentOutcome.class)))
                .thenReturn(CompletableFuture.completedFuture(org.mockito.Mockito.mock(SendResult.class)));
        when(paymentOutcomeRepository.save(any(PaymentOutcomeEntity.class))).thenAnswer(invocation -> {
            PaymentOutcomeEntity entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });
    }

    @Test
    void sameDebitAndCreditAccountResultsInRejectedOutcome() {
        PaymentEvent event = new PaymentEvent(UUID.randomUUID(), "ACC-1", "ACC-1", new BigDecimal("100.00"), "USD", "self transfer", Instant.parse("2026-04-18T10:00:00Z"));

        PaymentOutcome outcome = paymentProcessingService.process(event);

        assertThat(outcome.status()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(metricsService.getSummary().totalRejected()).isEqualTo(1);
        verify(kafkaTemplate).send(eq("payments.processed"), eq("ACC-1"), any(PaymentOutcome.class));
    }

    @Test
    void amountAboveThresholdResultsInHeldOutcome() {
        PaymentEvent event = new PaymentEvent(UUID.randomUUID(), "ACC-1", "ACC-2", new BigDecimal("250000.01"), "USD", "large payment", Instant.parse("2026-04-18T10:00:00Z"));

        PaymentOutcome outcome = paymentProcessingService.process(event);

        assertThat(outcome.status()).isEqualTo(PaymentStatus.HELD);
        assertThat(metricsService.getSummary().totalHeld()).isEqualTo(1);
        verify(kafkaTemplate).send(eq("payments.processed"), eq("ACC-1"), any(PaymentOutcome.class));
    }

    @Test
    void normalPaymentResultsInProcessedOutcome() {
        PaymentEvent event = new PaymentEvent(UUID.randomUUID(), "ACC-1", "ACC-2", new BigDecimal("249999.99"), "USD", "normal payment", Instant.parse("2026-04-18T10:00:00Z"));

        PaymentOutcome outcome = paymentProcessingService.process(event);

        assertThat(outcome.status()).isEqualTo(PaymentStatus.PROCESSED);
        assertThat(metricsService.getSummary().totalProcessed()).isEqualTo(1);
        verify(kafkaTemplate).send(eq("payments.processed"), eq("ACC-1"), any(PaymentOutcome.class));
    }
}

