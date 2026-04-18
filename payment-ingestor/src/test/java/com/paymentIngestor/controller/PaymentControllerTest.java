package com.paymentIngestor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentIngestor.Exception.GlobalExceptionHandler;
import com.paymentIngestor.dto.AccountResponse;
import com.paymentIngestor.dto.PaymentAcceptedResponse;
import com.paymentIngestor.dto.PaymentRequest;
import com.paymentIngestor.Repository.AccountRepository;
import com.paymentIngestor.entity.AccountStatus;
import com.paymentIngestor.entity.AccountType;
import com.paymentIngestor.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    void createPaymentReturnsAccepted() throws Exception {
        PaymentAcceptedResponse response = new PaymentAcceptedResponse("11111111-1111-4111-8111-111111111111", "ACCEPTED");
        when(paymentService.processPayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.paymentId").value(response.paymentId()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void getAccountReturnsAccountDetails() throws Exception {
        AccountResponse response = new AccountResponse(
                "20-15-88/43917265",
                "Marcus T. Oyelaran",
                AccountType.PERSONAL,
                AccountStatus.ACTIVE,
                "GBP",
                java.time.LocalDate.parse("2018-03-15")
        );
        when(paymentService.getAccount("20-15-88/43917265")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{accountId}", "20-15-88/43917265"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("20-15-88/43917265"))
                .andExpect(jsonPath("$.accountName").value("Marcus T. Oyelaran"))
                .andExpect(jsonPath("$.accountType").value("PERSONAL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currency").value("GBP"));
    }

    @Test
    void invalidPaymentRequestReturnsAllValidationViolations() throws Exception {
        String invalidJson = """
                {
                  "paymentId": "not-a-uuid",
                  "debitAccountId": "",
                  "creditAccountId": "",
                  "amount": 0,
                  "currency": "US",
                  "reference": "this-reference-is-definitely-longer-than-thirty-five-characters",
                  "timestamp": "2999-01-01T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.violations[*].field", hasItem("paymentId")))
                .andExpect(jsonPath("$.violations[*].field", hasItem("debitAccountId")))
                .andExpect(jsonPath("$.violations[*].field", hasItem("creditAccountId")))
                .andExpect(jsonPath("$.violations[*].field", hasItem("amount")))
                .andExpect(jsonPath("$.violations[*].field", hasItem("currency")))
                .andExpect(jsonPath("$.violations[*].field", hasItem("reference")))
                .andExpect(jsonPath("$.violations[*].field", hasItem("timestamp")));
    }

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentId(UUID.randomUUID().toString());
        request.setDebitAccountId("20-15-88/43917265");
        request.setCreditAccountId("20-15-88/61082934");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("GBP");
        request.setReference("rent payment");
        request.setTimestamp(OffsetDateTime.parse("2026-04-17T10:15:30Z"));
        return request;
    }
}




