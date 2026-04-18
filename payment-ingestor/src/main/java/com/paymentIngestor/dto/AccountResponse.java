package com.paymentIngestor.dto;

import com.paymentIngestor.entity.AccountStatus;
import com.paymentIngestor.entity.AccountType;

import java.time.LocalDate;

public record AccountResponse(
        String accountId,
        String accountName,
        AccountType accountType,
        AccountStatus status,
        String currency,
        LocalDate openedDate
) {
}

