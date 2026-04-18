package com.paystream.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record AppKafkaProperties(Topics topics) {
    public record Topics(String paymentsSubmitted, String paymentsProcessed) {
    }
}

