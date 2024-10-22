package com.example.proxy_server.controller;

import com.example.proxy_server.service.LRUCache;
import com.example.proxy_server.service.ProxyService;
import com.example.proxy_server.service.RateLimiter;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@RestController
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final ProxyService proxyService; // Injecting ProxyService

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }


    private final LRUCache<String, String> cache = new LRUCache<>(100); // Example cache size
    private final RateLimiter rateLimiter = new RateLimiter(10, TimeUnit.MINUTES.toMillis(1)); // Example rate limit

    @GetMapping("/proxy")
    public String proxyRequest(@RequestParam String url, @RequestHeader("User-ID") String userId) {
        if (!rateLimiter.allowRequest(userId)) {
            logger.warn("Rate limit exceeded for User: " + userId);
            return "Rate limit exceeded. Try again later.";
        }

        // Check the cache first
        if (cache.containsKey(url)) {
            logger.info("Cache hit for URL: " + url);
            return cache.get(url); // Return cached response
        } else {
            logger.info("Cache miss for URL: " + url);
        }

        // If not in cache, fetch from actual host
        String response = proxyService.fetchFromHost(url);
        if (response != null && !response.contains("Failed")) {
            cache.put(url, response); // Store in cache if valid response
            logger.info("Stored in cache: " + url);
        }
        if (response != null && !response.contains("Error")) {
            cache.put(url, response); // Store in cache if it's a valid response
            logger.info("Stored in cache: " + url);
        }
        return response;
    }

}
