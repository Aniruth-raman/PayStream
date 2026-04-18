package com.paymentIngestor.service;

import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.dto.PaymentRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "payments.submitted";

    public PaymentService(AccountRepository accountRepository,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String processPayment(PaymentRequest request) {

        // 🔹 1. Check debit account exists
        Account debit = accountRepository.findById(request.getDebitAccountId())
                .orElseThrow(() -> new NotFoundException(
                        "Debit account not found: " + request.getDebitAccountId()));

        // 🔹 2. Check credit account exists
        Account credit = accountRepository.findById(request.getCreditAccountId())
                .orElseThrow(() -> new NotFoundException(
                        "Credit account not found: " + request.getCreditAccountId()));

        // 🔹 3. Check account status
        if (debit.getStatus() == AccountStatus.SUSPENDED) {
            throw new UnprocessableException(
                    "Account is suspended: " + debit.getAccountId());
        }

        if (credit.getStatus() == AccountStatus.SUSPENDED) {
            throw new UnprocessableException(
                    "Account is suspended: " + credit.getAccountId());
        }

        // 🔹 4. (Optional but HIGH VALUE) Duplicate check
        // if (paymentRepository.existsByPaymentId(request.getPaymentId())) {
        //     throw new ConflictException("Duplicate paymentId");
        // }

        // 🔹 5. Publish to Kafka
        kafkaTemplate.send(
                TOPIC,
                request.getDebitAccountId(), // partition key
                request
        );

        return request.getPaymentId().toString();
    }
}