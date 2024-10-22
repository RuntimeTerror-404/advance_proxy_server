package com.example.proxy_server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final ConcurrentHashMap<String, Long> requestCounts = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long timeWindow;

    public RateLimiter(int maxRequests, long timeWindow) {
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
    }

    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        requestCounts.putIfAbsent(userId, currentTime);
        long lastRequestTime = requestCounts.get(userId);

        if (currentTime - lastRequestTime > timeWindow) {
            requestCounts.put(userId, currentTime);
            return true; // Allow request and reset count
        } else if (requestCounts.size() < maxRequests) {
            return true; // Allow request within window
        } else {
            return false; // Deny request
        }
    }
}

