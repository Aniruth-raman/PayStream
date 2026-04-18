package com.paystream.processor.service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import com.paystream.processor.dto.MetricsSummaryResponse;
import com.paystream.processor.entity.PaymentStatus;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final LongAdder totalProcessed = new LongAdder();
    private final LongAdder totalHeld = new LongAdder();
    private final LongAdder totalRejected = new LongAdder();
    private final AtomicLong totalProcessingTimeMs = new AtomicLong();

    public void increment(PaymentStatus status, long processingTimeMs) {
        switch (status) {
            case PROCESSED -> totalProcessed.increment();
            case HELD -> totalHeld.increment();
            case REJECTED -> totalRejected.increment();
        }
        totalProcessingTimeMs.addAndGet(Math.max(processingTimeMs, 0L));
    }

    public MetricsSummaryResponse getSummary() {
        long processed = totalProcessed.sum();
        long held = totalHeld.sum();
        long rejected = totalRejected.sum();
        long totalEvents = processed + held + rejected;
        double avgProcessingTimeMs = totalEvents == 0 ? 0.0d : (double) totalProcessingTimeMs.get() / totalEvents;
        return new MetricsSummaryResponse(processed, held, rejected, avgProcessingTimeMs);
    }
}

