package com.paystream.processor.controller;

import com.paystream.processor.dto.PaymentOutcomeResponse;
import com.paystream.processor.service.ReportingService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final ReportingService reportingService;

    @GetMapping("/{accountId}/history")
    public List<PaymentOutcomeResponse> history(@PathVariable @NotBlank String accountId) {
        return reportingService.getAccountHistory(accountId);
    }
}

