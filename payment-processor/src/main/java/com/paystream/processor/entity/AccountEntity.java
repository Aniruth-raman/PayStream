package com.paystream.processor.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "account_id", nullable = false, length = 64)
    private String accountId;

    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "opened_date", nullable = false)
    private LocalDate openedDate;

    public AccountEntity() {
    }

    public AccountEntity(String accountId, String accountName, AccountType accountType, AccountStatus status,
                         String currency, LocalDate openedDate) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.status = status;
        this.currency = currency;
        this.openedDate = openedDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(LocalDate openedDate) {
        this.openedDate = openedDate;
    }
}

