package com.transactions.banking_demo.handlers;

import com.transactions.banking_demo.dto.CreateTransactionRequest;
import com.transactions.banking_demo.dto.UpdateTransactionStatusRequest;
import com.transactions.banking_demo.entities.Transaction;
import com.transactions.banking_demo.entities.TransactionStatus;
import com.transactions.banking_demo.entities.TransactionType;
import com.transactions.banking_demo.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionHandler {

    private final TransactionService transactionService;
    private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    public Mono<ServerResponse> createTransaction(ServerRequest request) {
        return request.bodyToMono(CreateTransactionRequest.class)
                .flatMap(transactionService::createTransaction)
                .flatMap(transaction -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(transaction))
                .onErrorResume(error -> {
                    log.error("Error creating transaction: {}", error.getMessage());
                    return ServerResponse.badRequest()
                            .bodyValue("Error creating transaction: " + error.getMessage());
                });
    }

    public Mono<ServerResponse> getTransactionById(ServerRequest request) {
        String id = request.pathVariable("id");
        return transactionService.getTransactionById(id)
                .flatMap(transaction -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(transaction))
                .onErrorResume(error -> {
                    log.error("Error retrieving transaction {}: {}", id, error.getMessage());
                    return ServerResponse.notFound().build();
                });
    }

    public Mono<ServerResponse> getAllTransactions(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(transactionService.getAllTransactions(), Transaction.class);
    }

    public Mono<ServerResponse> getTransactionsByStatus(ServerRequest request) {
        try {
            String statusParam = request.queryParam("status").orElse("");
            TransactionStatus status = TransactionStatus.valueOf(statusParam.toUpperCase());

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(transactionService.getTransactionsByStatus(status), Transaction.class);
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid status parameter. Valid values: PENDING, POSTED, FAILED");
        }
    }

    public Mono<ServerResponse> getTransactionsByType(ServerRequest request) {
        try {
            String typeParam = request.queryParam("type").orElse("");
            TransactionType type = TransactionType.valueOf(typeParam.toUpperCase());

            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(transactionService.getTransactionsByType(type), Transaction.class);
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest()
                    .bodyValue("Invalid type parameter. Valid values: CASH_IN, CASH_OUT");
        }
    }

    public Mono<ServerResponse> updateTransactionStatus(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(UpdateTransactionStatusRequest.class)
                .flatMap(updateRequest -> transactionService.updateTransactionStatus(id, updateRequest))
                .flatMap(transaction -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(transaction))
                .onErrorResume(error -> {
                    log.error("Error updating transaction {}: {}", id, error.getMessage());
                    return ServerResponse.badRequest()
                            .bodyValue("Error updating transaction: " + error.getMessage());
                });
    }

    public Mono<ServerResponse> deleteTransaction(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return transactionService.deleteTransaction(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(error -> {
                    log.error("Error deleting transaction {}: {}", id, error.getMessage());
                    return ServerResponse.notFound().build();
                });
    }
}