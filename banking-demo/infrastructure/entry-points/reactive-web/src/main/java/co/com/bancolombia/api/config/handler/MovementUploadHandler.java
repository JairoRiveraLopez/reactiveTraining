package co.com.bancolombia.api.config.handler;

import co.com.bancolombia.usecase.uploadmovements.UploadMovementsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Log
public class MovementUploadHandler {

    private final UploadMovementsUseCase useCase;

    public Mono<ServerResponse> upload(ServerRequest req) {

        String boxId = req.pathVariable("boxId");
        String user = req.headers().firstHeader("x-user");

        return req.multipartData()
                .flatMap(parts -> Mono.justOrEmpty(parts.getFirst("file")))
                .cast(FilePart.class)
                .flatMap(fp -> {

                    if (!fp.headers().getContentType().toString().startsWith("text/csv")) {
                        return Mono.error(new IllegalArgumentException("Content-Type debe ser text/csv"));
                    }

                    log.info(() -> "📊 Procesando archivo CSV - Tamaño reportado: " + fp.headers().getContentLength() + " bytes");

                    Mono<String> fileContent = fp.content()
                            .collectList()
                            .map(buffers -> {
                                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                                    for (DataBuffer buffer : buffers) {
                                        byte[] bytes = new byte[buffer.readableByteCount()];
                                        buffer.read(bytes);
                                        outputStream.write(bytes);
                                        DataBufferUtils.release(buffer);
                                    }
                                    return outputStream.toString(StandardCharsets.UTF_8);
                                } catch (IOException e) {
                                    throw new RuntimeException("Error al leer el archivo", e);
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic()); // Procesar en thread separado

                    AtomicInteger lineCounter = new AtomicInteger(0);

                    Flux<String> lines = fileContent
                            .doOnNext(content -> log.info(() -> "📄 Contenido total leído: " + content.length() + " caracteres"))
                            .map(csv -> csv.replaceAll("\\r\\n?", "\n"))
                            .map(csv -> csv.split("\n", -1))
                            .doOnNext(arr -> log.info(() -> "📋 Total de líneas en el archivo: " + arr.length))
                            .flatMapMany(Flux::fromArray)
                            .filter(line -> !line.trim().isEmpty())
                            .doOnNext(line -> {
                                int count = lineCounter.incrementAndGet();
                                log.info(() -> String.format("📥 [Línea %d] Box event → %s", count, line));
                            });

                    return useCase.execute(
                                    boxId,
                                    lines,
                                    fp.headers().getContentLength(),
                                    user)
                            .doOnSuccess(report -> log.info(() -> "✅ Procesamiento completado: " + report));
                })
                .flatMap(report -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(report))
                .doOnError(e -> log.severe(() -> "Error en upload: " + e.getMessage()));
    }
}