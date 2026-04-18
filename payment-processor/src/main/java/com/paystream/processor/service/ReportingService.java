package com.paystream.processor.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.paystream.processor.dto.MetricsSummaryResponse;
import com.paystream.processor.dto.PagedResponse;
import com.paystream.processor.dto.PaymentOutcomeResponse;
import com.paystream.processor.dto.ReportSummaryResponse;
import com.paystream.processor.entity.AccountEntity;
import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import com.paystream.processor.repository.AccountRepository;
import com.paystream.processor.repository.PaymentOutcomeRepository;
import com.paystream.processor.repository.PaymentOutcomeSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);

    private final PaymentOutcomeRepository paymentOutcomeRepository;
    private final AccountRepository accountRepository;
    private final MetricsService metricsService;
    private final PaymentMapper paymentMapper;


    @Transactional(readOnly = true)
    public MetricsSummaryResponse getMetricsSummary() {
        log.info("Generating live metrics summary from in-memory counters.");
        return metricsService.getSummary();
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse getReportSummary() {
        log.info("Generating outcome report summary from the database.");
        long processed = paymentOutcomeRepository.countByStatus(PaymentStatus.PROCESSED);
        long held = paymentOutcomeRepository.countByStatus(PaymentStatus.HELD);
        long rejected = paymentOutcomeRepository.countByStatus(PaymentStatus.REJECTED);
        BigDecimal totalAmountProcessed = paymentOutcomeRepository.sumAmountByStatus(PaymentStatus.PROCESSED);
        Instant earliestProcessedAt = paymentOutcomeRepository.findEarliestProcessedAtByStatus(PaymentStatus.PROCESSED);
        Instant latestProcessedAt = paymentOutcomeRepository.findLatestProcessedAtByStatus(PaymentStatus.PROCESSED);
        return new ReportSummaryResponse(
                processed,
                held,
                rejected,
                totalAmountProcessed != null ? totalAmountProcessed : BigDecimal.ZERO,
                earliestProcessedAt,
                latestProcessedAt);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentOutcomeResponse> getActivity(PaymentStatus status, String accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "processedAt"));
        Specification<PaymentOutcomeEntity> specification = PaymentOutcomeSpecifications.hasStatus(status)
                .and(PaymentOutcomeSpecifications.involvesAccount(accountId));

        Page<PaymentOutcomeEntity> results = paymentOutcomeRepository.findAll(specification, pageable);
        log.info("Report query activity returned {} rows for status={} accountId={} page={} size={}",
                results.getNumberOfElements(), status, accountId, page, size);
        return toPagedResponse(results);
    }

    @Transactional(readOnly = true)
    public List<PaymentOutcomeResponse> getAccountHistory(String accountId) {
        log.info("Fetching payment history for accountId={}", accountId);
        List<PaymentOutcomeEntity> outcomes = paymentOutcomeRepository.findAll(
                PaymentOutcomeSpecifications.involvesAccount(accountId),
                Sort.by(Sort.Direction.DESC, "processedAt"));
        return toResponses(outcomes);
    }

    private PagedResponse<PaymentOutcomeResponse> toPagedResponse(Page<PaymentOutcomeEntity> pageResult) {
        List<PaymentOutcomeResponse> content = toResponses(pageResult.getContent());
        return new PagedResponse<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    private List<PaymentOutcomeResponse> toResponses(List<PaymentOutcomeEntity> outcomes) {
        return outcomes.stream()
                .map(outcome -> paymentMapper.toResponse(outcome, findDebitAccount(outcome.getDebitAccountId())))
                .toList();
    }

    private AccountEntity findDebitAccount(String accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }
}


