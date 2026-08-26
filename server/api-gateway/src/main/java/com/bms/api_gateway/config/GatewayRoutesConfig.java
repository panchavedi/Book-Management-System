package com.bms.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions.dedupeResponseHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> libraryServiceRoute() {
        return route("library-service")
            .route(
                path("/books")
                    .or(path("/books/**"))
                    .or(path("/borrow"))
                    .or(path("/borrow/**")),
                http()
            )
                .filter(lb("LIBRARY-SERVICE"))
                .after(dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
                .route(path("/api/auth").or(path("/api/auth/**")), http())
                .filter(lb("USERS-ERVICE"))
                .after(dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials"))
                .build();
    }
}
