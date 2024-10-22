package com.example.proxy_server.service;

import com.example.proxy_server.controller.ProxyController;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

public class MetricsLogger {

    private final MeterRegistry meterRegistry;
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    public MetricsLogger(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 60000) // Log metrics every minute
    public void logMetrics() {
        double latency = meterRegistry.get("proxy.request.latency").timer().mean(TimeUnit.MILLISECONDS);
        double errorCount = meterRegistry.get("proxy.request.errors").counter().count();

        logger.info("Average Request Latency: {} ms", latency);
        logger.info("Total Error Count: {}", errorCount);
    }
}
