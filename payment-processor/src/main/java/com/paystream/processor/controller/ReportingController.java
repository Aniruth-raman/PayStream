package com.paystream.processor.controller;

import com.paystream.processor.dto.PagedResponse;
import com.paystream.processor.dto.PaymentOutcomeResponse;
import com.paystream.processor.dto.ReportSummaryResponse;
import com.paystream.processor.entity.PaymentStatus;
import com.paystream.processor.service.ReportingService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Validated
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;


    @GetMapping("/summary")
    public ReportSummaryResponse summary() {
        return reportingService.getReportSummary();
    }

    @GetMapping("/activity")
    public PagedResponse<PaymentOutcomeResponse> activity(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String accountId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return reportingService.getActivity(status, accountId, page, size);
    }
}


