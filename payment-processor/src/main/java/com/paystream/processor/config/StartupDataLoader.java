package com.paystream.processor.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.processor.entity.AccountEntity;
import com.paystream.processor.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StartupDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupDataLoader.class);

    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    public StartupDataLoader(AccountRepository accountRepository, ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (accountRepository.count() > 0) {
            log.info("Account table already contains data; skipping startup seed load.");
            return;
        }

        ClassPathResource resource = new ClassPathResource("accounts.json");
        try (InputStream inputStream = resource.getInputStream()) {
            List<AccountEntity> accounts = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            accountRepository.saveAll(accounts);
            log.info("Loaded {} accounts into the payment-processor database.", accounts.size());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load accounts.json from the classpath", ex);
        }
    }
}

