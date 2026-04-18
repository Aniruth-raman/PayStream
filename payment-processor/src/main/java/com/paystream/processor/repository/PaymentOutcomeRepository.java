package com.paystream.processor.repository;

import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentOutcomeRepository extends JpaRepository<PaymentOutcomeEntity, Long>, JpaSpecificationExecutor<PaymentOutcomeEntity> {

    List<PaymentOutcomeEntity> findByDebitAccountIdOrCreditAccountIdOrderByProcessedAtDesc(String debitAccountId, String creditAccountId);

    long countByStatus(PaymentStatus status);

    @Query("select sum(p.amount) from PaymentOutcomeEntity p where p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("select min(p.processedAt) from PaymentOutcomeEntity p where p.status = :status")
    Instant findEarliestProcessedAtByStatus(@Param("status") PaymentStatus status);

    @Query("select max(p.processedAt) from PaymentOutcomeEntity p where p.status = :status")
    Instant findLatestProcessedAtByStatus(@Param("status") PaymentStatus status);
}


