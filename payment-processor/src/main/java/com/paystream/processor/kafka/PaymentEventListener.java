package com.paystream.processor.kafka;

import com.paystream.processor.config.AppKafkaProperties;
import com.paystream.processor.dto.PaymentEvent;
import com.paystream.processor.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final PaymentProcessingService paymentProcessingService;
    private final AppKafkaProperties appKafkaProperties;

    @KafkaListener(topics = "${app.kafka.topics.payments-submitted}", containerFactory = "paymentEventKafkaListenerContainerFactory")
    public void onMessage(PaymentEvent event) {
        try {
            log.info("Received payment event paymentId={} from topic={}", event.paymentId(), appKafkaProperties.topics().paymentsSubmitted());
            paymentProcessingService.process(event);
        } catch (Exception ex) {
            log.error("Failed to process paymentId={} from topic={}", event != null ? event.paymentId() : null, appKafkaProperties.topics().paymentsSubmitted(), ex);
            throw new IllegalStateException("Failed to process payment event " + (event != null ? event.paymentId() : "unknown"), ex);
        }
    }
}


