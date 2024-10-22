package com.example.proxy_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String check() {
        return "Server is running on port 8080";
    }

    // Add more service endpoints as needed
}

