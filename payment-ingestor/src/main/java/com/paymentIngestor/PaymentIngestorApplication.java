package com.paymentIngestor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@SpringBootApplication
public class PaymentIngestorApplication implements CommandLineRunner {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ObjectMapper objectMapper;

	public static void main(String[] args) {
		SpringApplication.run(PaymentIngestorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Resource resource=new ClassPathResource("accounts.json");

		List<Account> accounts;
		try (InputStream inputStream = resource.getInputStream()) {
			 accounts = objectMapper.readValue(inputStream, new TypeReference<>() {});
			accountRepository.saveAll(accounts);
			log.info("Loaded {} accounts into the payment-processor database.", accounts.size());
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to load accounts.json from the classpath", ex);
		}

		accountRepository.saveAll(accounts);

		System.out.println(accountRepository.count());
	}
}
