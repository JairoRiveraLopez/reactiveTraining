package com.transactions.banking_demo.config;

import com.transactions.banking_demo.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        log.error("Global error handler caught exception: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex);
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String errorMessage = createErrorMessage(ex, status);
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof RuntimeException && ex.getMessage().contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.REQUEST_TIMEOUT;
        }
        if (ex instanceof WebClientResponseException) {
            org.springframework.web.reactive.function.client.WebClientResponseException webEx =
                    (WebClientResponseException) ex;
            return HttpStatus.valueOf(webEx.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String createErrorMessage(Throwable ex, HttpStatus status) {
        Map<String, Object> errorAttributes = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error occurred",
                "path", "unknown"
        );

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(errorAttributes);
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return "{\"error\":\"Internal server error\"}";
        }
    }
}