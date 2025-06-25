package com.transactions.banking_demo.routers;

import com.transactions.banking_demo.handlers.LedgerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class LedgerRouter {

    @Bean
    public RouterFunction<ServerResponse> ledgerRoutes(LedgerHandler handler) {
        return RouterFunctions
                .route(POST("/ledger/process")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::processTransaction)
                .andRoute(GET("/ledger/health"), handler::healthCheck);
    }
}