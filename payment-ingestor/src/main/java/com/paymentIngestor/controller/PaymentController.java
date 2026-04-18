package com.paymentIngestor.controller;

import com.paymentIngestor.dto.AccountResponse;
import com.paymentIngestor.dto.PaymentAcceptedResponse;
import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<PaymentAcceptedResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentAcceptedResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/accounts/{*accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountId) {
        String normalizedAccountId = accountId.startsWith("/") ? accountId.substring(1) : accountId;
        return ResponseEntity.ok(paymentService.getAccount(normalizedAccountId));
    }
}