package com.example.proxy_server.service;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DynamicRouteService {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private ApplicationEventPublisher publisher;

    // Add new route
    public String add(RouteDefinition definition) {
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return "Route added successfully";
        } catch (Exception e) {
            return "Error adding route: " + e.getMessage();
        }
    }

    // Delete route
    public String delete(String routeId) {
        try {
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return "Route deleted successfully";
        } catch (Exception e) {
            return "Error deleting route: " + e.getMessage();
        }
    }
}

