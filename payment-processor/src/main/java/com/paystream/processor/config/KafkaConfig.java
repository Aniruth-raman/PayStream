package com.paystream.processor.config;

import java.util.HashMap;
import java.util.Map;

import com.paystream.processor.dto.PaymentEvent;
import com.paystream.processor.dto.PaymentOutcome;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.listener.concurrency:6}")
    private int listenerConcurrency;

    @Value("${spring.kafka.listener.ack-mode:BATCH}")
    private String ackMode;

    @Value("${spring.kafka.listener.auto-startup:true}")
    private boolean autoStartup;

    @Bean
    public ConsumerFactory<String, PaymentEvent> paymentEventConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.paystream.processor.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> paymentEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentEvent> paymentEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentEventConsumerFactory);
        factory.setConcurrency(listenerConcurrency);
        factory.setAutoStartup(autoStartup);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.valueOf(ackMode));
        return factory;
    }

    @Bean
    public ProducerFactory<String, PaymentOutcome> paymentOutcomeProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, PaymentOutcome> paymentOutcomeKafkaTemplate(
            ProducerFactory<String, PaymentOutcome> paymentOutcomeProducerFactory) {
        return new KafkaTemplate<>(paymentOutcomeProducerFactory);
    }
}

