package com.paymentIngestor.service;

import com.paymentIngestor.Exception.DuplicateAccountException;
import com.paymentIngestor.Exception.NotFoundException;
import com.paymentIngestor.Exception.UnprocessableException;
import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.Repository.PaymentRepository;
import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.entity.Account;
import com.paymentIngestor.entity.AccountStatus;
import com.paymentIngestor.entity.PaymentRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;


    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    private static final String TOPIC = "payments.submitted";

    public String processPayment(PaymentRequest request) throws Exception {

        // 🔹 1. Check debit account exists
        Account debit = accountRepository.findById(request.getDebitAccountId())
                .orElseThrow(() -> new Exception(
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

//        // 🔹 4. (Optional but HIGH VALUE) Duplicate check
//         if (paymentRepository.existsByPaymentId(request.getPaymentId())) {
//             throw new ConflictException("Duplicate paymentId");
//         }

        if (paymentRepository.existsById(request.getPaymentId())) {
            throw new DuplicateAccountException("Duplicate paymentId");
        }

        PaymentRequestEntity entity = PaymentRequestEntity.builder()
                .paymentId(request.getPaymentId())
                .debitAccountId(request.getDebitAccountId())
                .creditAccountId(request.getCreditAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .reference(request.getReference())
                .timestamp(request.getTimestamp())
                .build();
        paymentRepository.save(entity);

        // 🔹 5. Publish to Kafka
        kafkaTemplate.send(
                TOPIC,
                request.getDebitAccountId(), // partition key
                request
        );

        return request.getPaymentId().toString();
    }
}