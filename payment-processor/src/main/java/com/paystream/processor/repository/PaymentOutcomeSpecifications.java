package com.paystream.processor.repository;

import com.paystream.processor.entity.PaymentOutcomeEntity;
import com.paystream.processor.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentOutcomeSpecifications {

    private PaymentOutcomeSpecifications() {
    }

    public static Specification<PaymentOutcomeEntity> hasStatus(PaymentStatus status) {
        return (root, query, criteriaBuilder) -> status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<PaymentOutcomeEntity> involvesAccount(String accountId) {
        return (root, query, criteriaBuilder) -> {
            if (accountId == null || accountId.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("debitAccountId"), accountId),
                    criteriaBuilder.equal(root.get("creditAccountId"), accountId));
        };
    }
}

