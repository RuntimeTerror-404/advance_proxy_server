package com.example.proxy_server.config;

import com.example.proxy_server.service.LRUCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public LRUCache<String, String> lruCache() {
        // Define the LRUCache bean with the desired size
        return new LRUCache<>(100); // Example size
    }
}
