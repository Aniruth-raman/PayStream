package com.paystream.processor.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.paystream.processor.dto.MetricsSummaryResponse;
import com.paystream.processor.dto.PagedResponse;
import com.paystream.processor.dto.PaymentOutcomeResponse;
import com.paystream.processor.dto.ReportSummaryResponse;
import com.paystream.processor.entity.AccountEntity;
import com.paystream.processor.entity.AccountStatus;
import com.paystream.processor.entity.AccountType;
import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import com.paystream.processor.repository.AccountRepository;
import com.paystream.processor.repository.PaymentOutcomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private PaymentOutcomeRepository paymentOutcomeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void summaryAggregationUsesRepositoryCountsAndTotals() {
        MetricsService metricsService = new MetricsService();
        PaymentMapper paymentMapper = new PaymentMapper();
        ReportingService reportingService = new ReportingService(paymentOutcomeRepository, accountRepository, metricsService, paymentMapper);

        when(paymentOutcomeRepository.countByStatus(PaymentStatus.PROCESSED)).thenReturn(7L);
        when(paymentOutcomeRepository.countByStatus(PaymentStatus.HELD)).thenReturn(2L);
        when(paymentOutcomeRepository.countByStatus(PaymentStatus.REJECTED)).thenReturn(1L);
        when(paymentOutcomeRepository.sumAmountByStatus(PaymentStatus.PROCESSED)).thenReturn(new BigDecimal("12345.67"));
        Instant earliest = Instant.parse("2026-04-18T09:00:00Z");
        Instant latest = Instant.parse("2026-04-18T10:00:00Z");
        when(paymentOutcomeRepository.findEarliestProcessedAtByStatus(PaymentStatus.PROCESSED)).thenReturn(earliest);
        when(paymentOutcomeRepository.findLatestProcessedAtByStatus(PaymentStatus.PROCESSED)).thenReturn(latest);

        ReportSummaryResponse summary = reportingService.getReportSummary();

        assertThat(summary.totalProcessed()).isEqualTo(7L);
        assertThat(summary.totalHeld()).isEqualTo(2L);
        assertThat(summary.totalRejected()).isEqualTo(1L);
        assertThat(summary.totalAmountProcessed()).isEqualByComparingTo("12345.67");
        assertThat(summary.earliestProcessedAt()).isEqualTo(earliest);
        assertThat(summary.latestProcessedAt()).isEqualTo(latest);
    }

    @Test
    void accountHistoryIsReturnedMostRecentFirst() {
        MetricsService metricsService = new MetricsService();
        PaymentMapper paymentMapper = new PaymentMapper();
        ReportingService reportingService = new ReportingService(paymentOutcomeRepository, accountRepository, metricsService, paymentMapper);

        AccountEntity account = new AccountEntity("ACC-1", "Example Account", AccountType.PERSONAL, AccountStatus.ACTIVE, "USD", java.time.LocalDate.of(2024, 1, 1));
        when(accountRepository.findById("ACC-1")).thenReturn(Optional.of(account));

        PaymentOutcomeEntity latest = new PaymentOutcomeEntity(2L, UUIDHolder.uuid("00000000-0000-0000-0000-000000000002"), "ACC-1", "ACC-9", new BigDecimal("20.00"), "USD", PaymentStatus.PROCESSED, Instant.parse("2026-04-18T10:10:00Z"), 4L);
        PaymentOutcomeEntity earlier = new PaymentOutcomeEntity(1L, UUIDHolder.uuid("00000000-0000-0000-0000-000000000001"), "ACC-1", "ACC-2", new BigDecimal("10.00"), "USD", PaymentStatus.HELD, Instant.parse("2026-04-18T09:10:00Z"), 5L);
        when(paymentOutcomeRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(latest, earlier));

        List<PaymentOutcomeResponse> history = reportingService.getAccountHistory("ACC-1");

        assertThat(history).extracting(PaymentOutcomeResponse::paymentId)
                .containsExactly(latest.getPaymentId(), earlier.getPaymentId());
        assertThat(history).extracting(PaymentOutcomeResponse::debitAccountName)
                .containsExactly("Example Account", "Example Account");
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(paymentOutcomeRepository).findAll(any(Specification.class), sortCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.DESC, "processedAt"));
    }

    private static final class UUIDHolder {
        private static java.util.UUID uuid(String value) {
            return java.util.UUID.fromString(value);
        }
    }
}


