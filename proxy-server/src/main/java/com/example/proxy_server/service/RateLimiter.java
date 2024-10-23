package com.example.proxy_server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final long maxRequests;
    private final long timeWindowMillis;
    private final ConcurrentHashMap<String, Long> userRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userTimestamps = new ConcurrentHashMap<>();

    public RateLimiter(long maxRequests, long timeWindowMillis) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
    }

    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userTimestamps.putIfAbsent(userId, currentTime);
        userRequests.putIfAbsent(userId, 0L);

        long lastRequestTime = userTimestamps.get(userId);
        long requests = userRequests.get(userId);

        if (currentTime - lastRequestTime > timeWindowMillis) {
            // Reset time window for the user
            userTimestamps.put(userId, currentTime);
            userRequests.put(userId, 1L); // First request in new window
            return true;
        } else if (requests < maxRequests) {
            // Increment request count in current time window
            userRequests.put(userId, requests + 1);
            return true;
        } else {
            // Deny the request
            return false;
        }
    }
}


