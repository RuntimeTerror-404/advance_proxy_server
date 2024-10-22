package com.example.proxy_server.controller;

import com.example.proxy_server.service.LRUCache;
import com.example.proxy_server.service.ProxyService;
import com.example.proxy_server.service.RateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

import java.util.concurrent.TimeUnit;

@RestController
public class ProxyController {

    private final Counter errorCounter;
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    private final ProxyService proxyService;
    private final MeterRegistry meterRegistry;
    private final LRUCache<String, String> cache;  // Injected LRUCache bean
    private final RateLimiter rateLimiter = new RateLimiter(10, TimeUnit.MINUTES.toMillis(1)); // Example rate limit

    // Injecting LRUCache as a constructor argument
    public ProxyController(ProxyService proxyService, MeterRegistry meterRegistry, LRUCache<String, String> cache) {
        this.proxyService = proxyService;
        this.meterRegistry = meterRegistry;
        this.cache = cache; // Assign the injected cache
        this.errorCounter = meterRegistry.counter("proxy.request.errors"); // Counter for error rate
    }

    @GetMapping("/proxy")
    public String proxyRequest(@RequestParam String url, @RequestHeader("User-ID") String userId) {
        Timer timer = meterRegistry.timer("proxy.requests"); // Timer for request latency
        return timer.record(() -> {
            // Rate limiting check
            if (!rateLimiter.allowRequest(userId)) {
                logger.warn("Rate limit exceeded for User: " + userId);
                return "Rate limit exceeded. Try again later.";
            }

            // Cache check
            if (cache.containsKey(url)) {
                logger.info("Cache hit for URL: " + url);
                return cache.get(url); // Return cached response
            } else {
                logger.info("Cache miss for URL: " + url);
            }

            // Fetch from actual host
            try {
                String response = proxyService.fetchFromHost(url);

                if (response != null && !response.contains("Error")) {
                    cache.put(url, response); // Store in cache if valid response
                    logger.info("Stored in cache: " + url);
                }
                return response;
            } catch (Exception e) {
                // Increment the error counter when an exception occurs
                errorCounter.increment();
                logger.error("Error processing request for URL: " + url, e);
                return "Error occurred while processing your request.";
            }
        });
    }
}
