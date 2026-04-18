package com.paymentIngestor.service;

import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.Repository.PaymentRepository;
import com.paymentIngestor.dto.AccountResponse;
import com.paymentIngestor.dto.PaymentAcceptedResponse;
import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.entity.Account;
import com.paymentIngestor.entity.AccountStatus;
import com.paymentIngestor.entity.PaymentSubmissionEntity;
import com.paymentIngestor.Exception.AccountNotFoundException;
import com.paymentIngestor.Exception.AccountSuspendedException;
import com.paymentIngestor.Exception.DuplicatePaymentException;
import com.paymentIngestor.kafka.PaymentEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private static final String TOPIC = "payments.submitted";

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final Clock clock;

    public PaymentService(AccountRepository accountRepository,
                          PaymentRepository paymentRepository,
                          KafkaTemplate<String, PaymentEvent> kafkaTemplate,
                          Clock clock) {
        this.accountRepository = accountRepository;
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    public PaymentAcceptedResponse processPayment(PaymentRequest request) {
        UUID paymentId = UUID.fromString(request.getPaymentId());

        // Duplicate detection happens first so idempotency is enforced before any side effects.
        if (paymentRepository.existsByPaymentId(paymentId)) {
            throw new DuplicatePaymentException("Duplicate paymentId");
        }

        Account debitAccount = accountRepository.findById(request.getDebitAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Debit account not found " + request.getDebitAccountId()));

        Account creditAccount = accountRepository.findById(request.getCreditAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Credit account not found " + request.getCreditAccountId()));

        if (debitAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("Account is suspended " + debitAccount.getAccountId());
        }

        if (creditAccount.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("Account is suspended " + creditAccount.getAccountId());
        }

        // We persist the paymentId before publishing so a retry cannot create a duplicate submission.
        // If the Kafka publish fails after this point, the request is considered submitted but not accepted.
        PaymentSubmissionEntity submission = PaymentSubmissionEntity.builder()
                .paymentId(paymentId)
                .createdAt(Instant.now(clock))
                .build();

        try {
            paymentRepository.saveAndFlush(submission);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicatePaymentException("Duplicate paymentId");
        }

        PaymentEvent event = new PaymentEvent(
                paymentId,
                request.getDebitAccountId(),
                request.getCreditAccountId(),
                request.getAmount(),
                request.getCurrency(),
                request.getReference(),
                request.getTimestamp()
        );

        kafkaTemplate.send(TOPIC, request.getDebitAccountId(), event).join();

        return new PaymentAcceptedResponse(paymentId.toString(), "ACCEPTED");
    }

    public AccountResponse getAccount(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found " + accountId));

        return new AccountResponse(
                account.getAccountId(),
                account.getAccountName(),
                account.getAccountType(),
                account.getStatus(),
                account.getCurrency(),
                account.getOpenedDate()
        );
    }
}