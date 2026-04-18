package com.paystream.processor.controller;

import com.paystream.processor.dto.MetricsSummaryResponse;
import com.paystream.processor.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final ReportingService reportingService;

    @GetMapping("/summary")
    public MetricsSummaryResponse getSummary() {
        return reportingService.getMetricsSummary();
    }
}

