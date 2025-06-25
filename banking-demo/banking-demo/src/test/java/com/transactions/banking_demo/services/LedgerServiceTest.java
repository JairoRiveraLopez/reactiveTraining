package com.transactions.banking_demo.services;

import com.transactions.banking_demo.dto.LedgerRequest;
import com.transactions.banking_demo.dto.LedgerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.net.ConnectException;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    private static final UUID TX_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    ExchangeFunction exchangeFunction;

    @Mock
    LedgerRequest request;

    LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        ledgerService = new LedgerService(builder, "http://dummy-ledger");
    }

    private static ClientResponse okJson(String body) {
        return ClientResponse
                .create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build();
    }

    @Nested
    @DisplayName("Camino feliz – primera llamada exitosa")
    class SuccessPath {

        @Test
        void processTransaction_ok() {
            String body = """
                    {"transactionId":"%s","success":true,"message":"OK"}
                    """.formatted(TX_ID);
            when(exchangeFunction.exchange(any(ClientRequest.class)))
                    .thenReturn(Mono.just(okJson(body)));

            Mono<LedgerResponse> mono = ledgerService.processTransaction(request);

            StepVerifier.create(mono)
                    .assertNext(res -> {
                        assertThat(res.isSuccess()).isTrue();
                        assertThat(res.getTransactionId()).isEqualTo(TX_ID);
                        assertThat(res.getMessage()).isEqualTo("OK");
                    })
                    .verifyComplete();

            verify(exchangeFunction, times(1)).exchange(any(ClientRequest.class));
        }
    }

    @Nested
    @DisplayName("Reintento – primer fallo recuperable y segundo éxito")
    class RetrySuccessPath {

        @Test
        void processTransaction_retry_then_ok() {
            String body = """
                    {"transactionId":"%s","success":true,"message":"OK after retry"}
                    """.formatted(TX_ID);
            when(exchangeFunction.exchange(any(ClientRequest.class)))
                    .thenReturn(Mono.error(new ConnectException("refused")))
                    .thenReturn(Mono.just(okJson(body)));

            Mono<LedgerResponse> mono = ledgerService.processTransaction(request);

            StepVerifier.create(mono)
                    .assertNext(res -> {
                        assertThat(res.isSuccess()).isTrue();
                        assertThat(res.getMessage()).isEqualTo("OK after retry");
                    })
                    .verifyComplete();

            verify(exchangeFunction, times(2)).exchange(any(ClientRequest.class));
        }
    }

    @Nested
    @DisplayName("Reintentos agotados – fallback de errorResume")
    class RetryExhaustedPath {

        @Test
        void processTransaction_retry_exhausted() {
            // El fallback necesita el transactionId:
            when(request.getTransactionId()).thenReturn(TX_ID);

            when(exchangeFunction.exchange(any(ClientRequest.class)))
                    .thenAnswer(inv -> Mono.error(new ConnectException("boom"))); // 4 veces

            Mono<LedgerResponse> mono = ledgerService.processTransaction(request);

            StepVerifier.create(mono)
                    .assertNext(res -> {
                        assertThat(res.isSuccess()).isFalse();
                        assertThat(res.getTransactionId()).isEqualTo(TX_ID);
                        assertThat(res.getMessage()).startsWith("Ledger processing failed:");
                    })
                    .verifyComplete();

            verify(exchangeFunction, times(4)).exchange(any(ClientRequest.class));
        }
    }

    @Nested
    @DisplayName("Timeout – onErrorReturn")
    class TimeoutPath {

        @Test
        void processTransaction_timeout() {
            when(request.getTransactionId()).thenReturn(TX_ID);
            when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.never());

            VirtualTimeScheduler.getOrSet();          // evita 10 s reales
            Mono<LedgerResponse> mono = ledgerService.processTransaction(request);

            StepVerifier.withVirtualTime(() -> mono)
                    .thenAwait(Duration.ofSeconds(10))
                    .assertNext(res -> {
                        assertThat(res.isSuccess()).isFalse();
                        assertThat(res.getTransactionId()).isEqualTo(TX_ID);
                        assertThat(res.getMessage()).isEqualTo("Ledger service timeout");
                    })
                    .verifyComplete();

            verify(exchangeFunction).exchange(any(ClientRequest.class));
        }
    }

    @Test
    @DisplayName("isRetryableException devuelve true para los tipos soportados")
    void isRetryableException_supportedTypes() throws Exception {
        var method = LedgerService.class
                .getDeclaredMethod("isRetryableException", Throwable.class);
        method.setAccessible(true);

        assertThat(method.invoke(ledgerService, new ConnectException())).isEqualTo(true);
        assertThat(method.invoke(ledgerService,
                org.springframework.web.reactive.function.client.WebClientResponseException.create(
                        502, "Bad Gateway", HttpHeaders.EMPTY, new byte[0], null)))
                .isEqualTo(true);
        assertThat(method.invoke(ledgerService,
                org.springframework.web.reactive.function.client.WebClientResponseException.create(
                        503, "Service Unavailable", HttpHeaders.EMPTY, new byte[0], null)))
                .isEqualTo(true);
        assertThat(method.invoke(ledgerService,
                new java.util.concurrent.TimeoutException())).isEqualTo(true);
        assertThat(method.invoke(ledgerService,
                new RuntimeException())).isEqualTo(false);
    }

    @Test
    void processTransaction_callsCorrectEndpoint() {
        String body = """
                {"transactionId":"%s","success":true,"message":"checkUri"}
                """.formatted(TX_ID);
        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        when(exchangeFunction.exchange(captor.capture()))
                .thenReturn(Mono.just(okJson(body)));

        ledgerService.processTransaction(request).block();

        assertThat(captor.getValue().url().getPath()).isEqualTo("/ledger/process");
    }
}
