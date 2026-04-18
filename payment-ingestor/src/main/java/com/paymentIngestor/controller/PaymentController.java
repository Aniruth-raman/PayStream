package com.paymentIngestor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        String paymentId = paymentService.processPayment(request);

        return ResponseEntity
                .accepted()
                .body(Map.of("paymentId", paymentId));
    }
}