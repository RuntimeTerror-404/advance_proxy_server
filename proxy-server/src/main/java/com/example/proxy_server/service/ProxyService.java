package com.example.proxy_server.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class ProxyService {

    private final LRUCache<String, String> cache;
    private final MeterRegistry meterRegistry;

    public ProxyService(LRUCache<String, String> cache, MeterRegistry meterRegistry) {
        this.cache = cache;
        this.meterRegistry = meterRegistry;
    }

    public String fetchData(String url) {
        if (cache.containsKey(url)) {
            meterRegistry.counter("cache.hits").increment(); // Increment cache hit counter
            return cache.get(url);
        } else {
            meterRegistry.counter("cache.misses").increment(); // Increment cache miss counter
            String response = fetchFromHost(url);
            cache.put(url, response);
            return response;
        }
    }

    public String fetchFromHost(String url) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "Failed to fetch from host: " + e.getMessage();
        }
    }
}
