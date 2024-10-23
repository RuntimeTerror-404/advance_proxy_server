package com.example.proxy_server.controller;

import com.example.proxy_server.service.GeoLocationService;
import com.example.proxy_server.service.LRUCache;
import com.example.proxy_server.service.ProxyService;
import com.example.proxy_server.service.RateLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
public class ProxyController {

    private final Counter errorCounter;
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    private final ProxyService proxyService;
    private final MeterRegistry meterRegistry;
    private final LRUCache<String, String> cache;  // Injected LRUCache bean
    private final RateLimiter rateLimiter = new RateLimiter(10, TimeUnit.MINUTES.toMillis(1));

    private final Set<String> whitelist = new HashSet<>(); // Change to HashSet for faster lookups
    private final Set<String> blacklist = new HashSet<>(); // Change to HashSet for faster lookups// Example rate limit
    private final GeoLocationService geoLocationService; // Add GeoLocationService
    private final Set<String> blockedCountries = new HashSet<>(Arrays.asList("CN", "RU", "IQ", "SY", "CU", "NL")); // Example blocked countries

    // Injecting LRUCache as a constructor argument
    public ProxyController(ProxyService proxyService, MeterRegistry meterRegistry, LRUCache<String, String> cache, GeoLocationService geoLocationService) {
        this.proxyService = proxyService;
        this.meterRegistry = meterRegistry;
        this.cache = cache; // Assign the injected cache
        this.errorCounter = meterRegistry.counter("proxy.request.errors"); // Counter for error rate
        this.geoLocationService = geoLocationService; // Inject GeoLocationService

        // Example entries; you can load these from a config file or database
//        whitelist.add("127.0.0.1"); // Localhost
        whitelist.add("104.28.220.169"); // Localhost: using public ip of current location
        blacklist.add("192.168.1.200");
    }

    @GetMapping("/proxy")
    public String proxyRequest(@RequestParam String url, @RequestHeader("User-ID") String userId, ServerHttpRequest request) {
        Timer timer = meterRegistry.timer("proxy.requests"); // Timer for request latency
        String clientIp = request.getRemoteAddress().getAddress().getHostAddress();
        logger.info("Client IP: " + clientIp);


        return timer.record(() -> {
            // Rate limiting check
            if (!rateLimiter.allowRequest(userId)) {
                logger.warn("Rate limit exceeded for User: " + userId);
                return "Rate limit exceeded. Try again later.";
            } else {
                logger.info("Rate limit check passed for User: " + userId);
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
                // Fetch public IP
                String publicIp = fetchPublicIp();

                // Get country code using the public IP
                String countryCode = geoLocationService.getCountryCode(publicIp);

                // Geo-blocking logic
                if (blockedCountries.contains(countryCode)) {
                    logger.info("public IP: " + publicIp);
                    return "Access denied: Requests from your country are blocked.";
                }

                // Check if the client IP is blacklisted
                if (isBlacklisted(publicIp)) {
                    logger.warn("Blocked request from blacklisted IP: " + publicIp);
                    return "Access denied: Your IP is blacklisted.";
                }

                // Check if the client IP is whitelisted
                if (!isWhitelisted(publicIp)) {
                    logger.warn("Blocked request from non-whitelisted IP: " + publicIp);
                    return "Access denied: Your IP is not whitelisted.";
                }


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
    private String fetchPublicIp() throws IOException {
        URL url = new URL("https://api.ipify.org");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String publicIp = in.readLine();
        in.close();
        return publicIp;
    }
    private boolean isBlacklisted(String ip) {
        return blacklist.contains(ip);
    }

    private boolean isWhitelisted(String ip) {
        return whitelist.contains(ip);
    }
}
