package com.transactions.banking_demo.handlers;

import com.transactions.banking_demo.config.GlobalErrorHandler;
import com.transactions.banking_demo.dto.LedgerRequest;
import com.transactions.banking_demo.dto.LedgerResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Component
public class LedgerHandler {

    private final Random random = new Random();
    private static final Logger log = LoggerFactory.getLogger(LedgerHandler.class);

    public Mono<ServerResponse> processTransaction(ServerRequest request) {
        return request.bodyToMono(LedgerRequest.class)
                .flatMap(this::simulateLedgerProcessing)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(error -> {
                    log.error("Error in ledger processing: {}", error.getMessage());
                    return ServerResponse.status(500)
                            .bodyValue(LedgerResponse.builder()
                                    .success(false)
                                    .message("Internal ledger error")
                                    .build());
                });
    }

    private Mono<LedgerResponse> simulateLedgerProcessing(LedgerRequest request) {
        log.info("Processing ledger request for transaction: {}", request.getTransactionId());

        return Mono.delay(Duration.ofMillis(100 + random.nextInt(500)))
                .map(tick -> {
                    boolean success = random.nextDouble() > 0.1;

                    if (random.nextDouble() < 0.05) {
                        throw new RuntimeException("Ledger service temporarily unavailable");
                    }

                    return LedgerResponse.builder()
                            .transactionId(request.getTransactionId())
                            .success(success)
                            .message(success ? "Transaction processed successfully" : "Transaction processing failed")
                            .build();
                })
                .doOnSuccess(response -> log.info("Ledger processing completed for transaction {}: success={}",
                        request.getTransactionId(), response.isSuccess()));
    }

    public Mono<ServerResponse> healthCheck(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.Map.of(
                        "status", "UP",
                        "service", "ledger",
                        "timestamp", java.time.Instant.now()
                ));
    }
}
