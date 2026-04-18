package com.paystream.processor.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentOutcomeRepositoryTest {

    @Autowired
    private PaymentOutcomeRepository paymentOutcomeRepository;

    @Test
    void findsOutcomesByDebitOrCreditAccountMostRecentFirst() {
        PaymentOutcomeEntity older = new PaymentOutcomeEntity(null, UUID.fromString("00000000-0000-0000-0000-000000000001"), "ACC-1", "ACC-9", new BigDecimal("11.00"), "USD", PaymentStatus.PROCESSED, Instant.parse("2026-04-18T09:00:00Z"), 5L);
        PaymentOutcomeEntity newer = new PaymentOutcomeEntity(null, UUID.fromString("00000000-0000-0000-0000-000000000002"), "ACC-9", "ACC-1", new BigDecimal("22.00"), "USD", PaymentStatus.HELD, Instant.parse("2026-04-18T10:00:00Z"), 7L);
        paymentOutcomeRepository.saveAll(List.of(older, newer));

        List<PaymentOutcomeEntity> results = paymentOutcomeRepository.findByDebitAccountIdOrCreditAccountIdOrderByProcessedAtDesc("ACC-1", "ACC-1");

        assertThat(results).extracting(PaymentOutcomeEntity::getPaymentId)
                .containsExactly(newer.getPaymentId(), older.getPaymentId());
    }
}

