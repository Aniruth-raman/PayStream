package com.paymentIngestor.controller;

import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody PaymentRequest request) throws Exception {

        String paymentId = paymentService.processPayment(request);

        return ResponseEntity
                .accepted()
                .body(Map.of("paymentId", paymentId));
    }
}