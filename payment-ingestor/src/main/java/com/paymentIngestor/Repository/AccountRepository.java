package com.paymentIngestor.Repository;

import com.paymentIngestor.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
