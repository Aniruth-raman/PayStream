package com.paystream.processor.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.paystream.processor.dto.PaymentEvent;
import com.paystream.processor.service.PaymentProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false"
})
class PaymentEventListenerIntegrationTest {

    @Autowired
    private PaymentEventListener paymentEventListener;

    @MockBean
    private PaymentProcessingService paymentProcessingService;

    @Test
    void listenerDelegatesPaymentEventToProcessingService() {
        PaymentEvent event = new PaymentEvent(UUID.randomUUID(), "ACC-1", "ACC-2", new BigDecimal("42.00"), "USD", "integration-test", Instant.parse("2026-04-18T10:00:00Z"));

        paymentEventListener.onMessage(event);

        verify(paymentProcessingService).process(event);
    }
}

