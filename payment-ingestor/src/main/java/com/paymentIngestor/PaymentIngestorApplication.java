package com.paymentIngestor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@SpringBootApplication
public class PaymentIngestorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentIngestorApplication.class, args);
	}

	@Bean
//	@ConditionalOnBean(AccountRepository.class)
	CommandLineRunner accountDataLoader(AccountRepository accountRepository, ObjectMapper objectMapper) {
		return args -> {
			if (accountRepository.count() > 0) {
				log.info("Accounts table already contains data; skipping seed load.");
				return;
			}

			Resource resource = new ClassPathResource("accounts.json");
			try (InputStream inputStream = resource.getInputStream()) {
				List<Account> accounts = objectMapper.readValue(inputStream, new TypeReference<>() {});
				accountRepository.saveAll(accounts);
				log.info("Loaded {} accounts into the payment-ingestor database.", accounts.size());
			} catch (IOException ex) {
				throw new IllegalStateException("Failed to load accounts.json from the classpath", ex);
			}
		};
	}
}
