package com.paymentIngestor.service;

import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.Repository.PaymentRepository;
import com.paymentIngestor.dto.PaymentAcceptedResponse;
import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.entity.Account;
import com.paymentIngestor.entity.AccountStatus;
import com.paymentIngestor.entity.AccountType;
import com.paymentIngestor.entity.PaymentSubmissionEntity;
import com.paymentIngestor.Exception.AccountNotFoundException;
import com.paymentIngestor.Exception.AccountSuspendedException;
import com.paymentIngestor.Exception.DuplicatePaymentException;
import com.paymentIngestor.kafka.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                accountRepository,
                paymentRepository,
                kafkaTemplate,
                Clock.fixed(Instant.parse("2026-04-18T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void validRequestPublishesKafkaMessageAndReturnsAcceptedResponse() {
        PaymentRequest request = request("11111111-1111-4111-8111-111111111111", "debit-1", "credit-1");
        Account debit = account("debit-1", AccountStatus.ACTIVE);
        Account credit = account("credit-1", AccountStatus.ACTIVE);

        when(paymentRepository.existsByPaymentId(UUID.fromString(request.getPaymentId()))).thenReturn(false);
        when(accountRepository.findById("debit-1")).thenReturn(Optional.of(debit));
        when(accountRepository.findById("credit-1")).thenReturn(Optional.of(credit));
        when(paymentRepository.saveAndFlush(any(PaymentSubmissionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(kafkaTemplate.send(eq("payments.submitted"), eq("debit-1"), any(PaymentEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        PaymentAcceptedResponse response = paymentService.processPayment(request);

        assertThat(response.paymentId()).isEqualTo(request.getPaymentId());
        assertThat(response.status()).isEqualTo("ACCEPTED");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(kafkaTemplate).send(eq("payments.submitted"), keyCaptor.capture(), eventCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo("debit-1");
        assertThat(eventCaptor.getValue().paymentId()).isEqualTo(UUID.fromString(request.getPaymentId()));
        assertThat(eventCaptor.getValue().debitAccountId()).isEqualTo("debit-1");
        verify(paymentRepository).saveAndFlush(any(PaymentSubmissionEntity.class));
    }

    @Test
    void duplicatePaymentIdReturnsConflict() {
        PaymentRequest request = request("11111111-1111-4111-8111-111111111111", "debit-1", "credit-1");
        when(paymentRepository.existsByPaymentId(UUID.fromString(request.getPaymentId()))).thenReturn(true);

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessage("Duplicate paymentId");

        verify(accountRepository, never()).findById(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void missingDebitAccountReturnsNotFound() {
        PaymentRequest request = request("11111111-1111-4111-8111-111111111111", "missing-debit", "credit-1");
        when(paymentRepository.existsByPaymentId(UUID.fromString(request.getPaymentId()))).thenReturn(false);
        when(accountRepository.findById("missing-debit")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Debit account not found missing-debit");

        verify(accountRepository, never()).findById("credit-1");
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void missingCreditAccountReturnsNotFound() {
        PaymentRequest request = request("11111111-1111-4111-8111-111111111111", "debit-1", "missing-credit");
        when(paymentRepository.existsByPaymentId(UUID.fromString(request.getPaymentId()))).thenReturn(false);
        when(accountRepository.findById("debit-1")).thenReturn(Optional.of(account("debit-1", AccountStatus.ACTIVE)));
        when(accountRepository.findById("missing-credit")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Credit account not found missing-credit");

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void suspendedDebitAccountReturnsUnprocessableEntity() {
        PaymentRequest request = request("11111111-1111-4111-8111-111111111111", "debit-1", "credit-1");
        when(paymentRepository.existsByPaymentId(UUID.fromString(request.getPaymentId()))).thenReturn(false);
        when(accountRepository.findById("debit-1")).thenReturn(Optional.of(account("debit-1", AccountStatus.SUSPENDED)));
        when(accountRepository.findById("credit-1")).thenReturn(Optional.of(account("credit-1", AccountStatus.ACTIVE)));

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(AccountSuspendedException.class)
                .hasMessage("Account is suspended debit-1");

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void suspendedCreditAccountReturnsUnprocessableEntity() {
        PaymentRequest request = request("11111111-1111-4111-8111-111111111111", "debit-1", "credit-1");
        when(paymentRepository.existsByPaymentId(UUID.fromString(request.getPaymentId()))).thenReturn(false);
        when(accountRepository.findById("debit-1")).thenReturn(Optional.of(account("debit-1", AccountStatus.ACTIVE)));
        when(accountRepository.findById("credit-1")).thenReturn(Optional.of(account("credit-1", AccountStatus.SUSPENDED)));

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(AccountSuspendedException.class)
                .hasMessage("Account is suspended credit-1");

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    private PaymentRequest request(String paymentId, String debitAccountId, String creditAccountId) {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentId(paymentId);
        request.setDebitAccountId(debitAccountId);
        request.setCreditAccountId(creditAccountId);
        request.setAmount(new BigDecimal("25.50"));
        request.setCurrency("GBP");
        request.setReference("invoice-123");
        request.setTimestamp(OffsetDateTime.parse("2026-04-17T12:00:00Z"));
        return request;
    }

    private Account account(String accountId, AccountStatus status) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setAccountName("Test Account");
        account.setAccountType(AccountType.PERSONAL);
        account.setStatus(status);
        account.setCurrency("GBP");
        account.setOpenedDate(java.time.LocalDate.of(2020, 1, 1));
        return account;
    }
}




