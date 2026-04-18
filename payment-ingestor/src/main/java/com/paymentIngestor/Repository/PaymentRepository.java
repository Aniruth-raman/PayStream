package com.paymentIngestor.Repository;

import com.paymentIngestor.entity.PaymentSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentSubmissionEntity, UUID> {

    boolean existsByPaymentId(UUID paymentId);
}
