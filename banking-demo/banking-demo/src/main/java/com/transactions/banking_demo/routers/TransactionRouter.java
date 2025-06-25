package com.transactions.banking_demo.routers;

import com.transactions.banking_demo.handlers.TransactionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class TransactionRouter {

    @Bean
    public RouterFunction<ServerResponse> transactionRoutes(TransactionHandler handler) {
        return RouterFunctions
                .route(POST("/api/transactions")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::createTransaction)
                .andRoute(GET("/api/transactions/{id}"), handler::getTransactionById)
                .andRoute(GET("/api/transactions")
                        .and(queryParam("status", status -> true)), handler::getTransactionsByStatus)
                .andRoute(GET("/api/transactions")
                        .and(queryParam("type", type -> true)), handler::getTransactionsByType)
                .andRoute(GET("/api/transactions"), handler::getAllTransactions)
                .andRoute(PUT("/api/transactions/{id}/status")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::updateTransactionStatus)
                .andRoute(DELETE("/api/transactions/{id}"), handler::deleteTransaction);
    }
}