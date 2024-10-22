package com.example.proxy_server.controller;
import com.example.proxy_server.service.DynamicRouteService;  // Assuming DynamicRouteService is in the 'service' package
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gateway")
public class RouteController {

    @Autowired
    private DynamicRouteService dynamicRouteService;

    @PostMapping("/addRoute")
    public ResponseEntity<String> addRoute(@RequestBody RouteDefinition definition) {
        String response = dynamicRouteService.add(definition);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteRoute/{id}")
    public ResponseEntity<String> deleteRoute(@PathVariable String id) {
        String response = dynamicRouteService.delete(id);
        return ResponseEntity.ok(response);
    }
}

