package com.transactions.banking_demo.services;

import com.transactions.banking_demo.dto.LedgerRequest;
import com.transactions.banking_demo.dto.LedgerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class LedgerService {

    private final WebClient webClient;

    public LedgerService(WebClient.Builder webClientBuilder,
                         @Value("${ledger.base-url:http://localhost:8080}") String ledgerBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(ledgerBaseUrl)
                .build();
    }

    public Mono<LedgerResponse> processTransaction(LedgerRequest request) {
        return webClient
                .post()
                .uri("/ledger/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LedgerResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5))
                        .filter(this::isRetryableException))
                .onErrorResume(throwable -> {
                    return Mono.just(LedgerResponse.builder()
                            .transactionId(request.getTransactionId())
                            .success(false)
                            .message("Ledger processing failed: " + throwable.getMessage())
                            .build());
                })
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(LedgerResponse.builder()
                        .transactionId(request.getTransactionId())
                        .success(false)
                        .message("Ledger service timeout")
                        .build());
    }

    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable ||
                throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.BadGateway ||
                throwable instanceof java.net.ConnectException ||
                throwable instanceof java.util.concurrent.TimeoutException;
    }
}