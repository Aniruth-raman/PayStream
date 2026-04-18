package com.paymentIngestor.Repository;

import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.entity.PaymentRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentRequestEntity, UUID> {

    boolean existsByPaymentId(UUID paymentId);
}
