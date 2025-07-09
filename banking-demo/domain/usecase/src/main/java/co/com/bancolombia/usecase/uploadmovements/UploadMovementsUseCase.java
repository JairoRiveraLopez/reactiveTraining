package co.com.bancolombia.usecase.uploadmovements;

import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxMovementsUploadedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.movement.MovementType;
import co.com.bancolombia.model.movement.gateways.MovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Log
public class UploadMovementsUseCase {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CURRENCIES = Set.of("COP", "USD");

    private final BoxRepository      boxRepo;
    private final MovementRepository movementRepo;
    private final EventsGateway      events;

    public Mono<UploadReport> execute(String boxId,
                                      Flux<String> lines,
                                      long sizeBytes,
                                      String uploadedBy) {

        if (sizeBytes > MAX_SIZE_BYTES)
            return Mono.error(new IllegalArgumentException("Archivo > 5 MB"));

        Mono<Void> boxExists = boxRepo.findById(boxId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Caja inexistente: " + boxId)))
                .then();

        AtomicLong total    = new AtomicLong();
        AtomicLong rejected = new AtomicLong();

        ConcurrentHashMap<String, Boolean> seenIds = new ConcurrentHashMap<>();

        Flux<Movement> validMovements = lines
                .skip(1)
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .doOnNext(l -> total.incrementAndGet())
                .flatMap(l -> parseLine(l, boxId)
                        .doOnError(e -> rejected.incrementAndGet())
                        .onErrorResume(e -> {
                            log.warning(() -> "Error en línea #" + total.get() + ": " + e.getMessage());
                            return Mono.empty();
                        })
                )
                .flatMap(mv -> {
                    if (seenIds.putIfAbsent(mv.getMovementId(), Boolean.TRUE) == null) {
                        return Mono.just(mv);
                    }
                    rejected.incrementAndGet();
                    log.warning(() -> "ID duplicado en CSV: " + mv.getMovementId());
                    return Mono.empty();
                }).doOnNext(mv -> log.info(() -> "✅ Movimiento válido: " + mv.getMovementId()));

        return boxExists
                .thenMany(movementRepo.saveAll(validMovements))
                .count()
                .map(success -> new UploadReport(total.get(), success, rejected.get()))
                .doOnNext(r -> log.info(() -> String.format(
                        "📊 Reporte final - Total: %d, Exitosos: %d, Fallidos: %d",
                        r.total(), r.success(), r.failed())))
                .flatMap(r -> events.emit(
                                new BoxMovementsUploadedEvent(
                                        boxId, r.total(), r.success(), r.failed(), uploadedBy))
                        .thenReturn(r)
                );
    }

    private Mono<Movement> parseLine(String line, String expectedBoxId) {
        try {
            String[] c = line.split(",", -1);
            if (c.length < 7)
                throw new IllegalArgumentException("columnas incompletas");

            String movementId = c[0].trim();
            if (movementId.isEmpty())
                throw new IllegalArgumentException("movementId vacío");

            if (!expectedBoxId.equals(c[1].trim()))
                throw new IllegalArgumentException("boxId distinto al de la URL");

            LocalDateTime date = LocalDateTime.parse(c[2].trim());

            MovementType type = MovementType.valueOf(c[3].trim());

            BigDecimal amount = new BigDecimal(c[4].trim());
            if (amount.signum() <= 0)
                throw new IllegalArgumentException("amount ≤ 0");

            String currency = c[5].trim();
            if (!ALLOWED_CURRENCIES.contains(currency))
                throw new IllegalArgumentException("currency inválida: " + currency);

            String description = c[6].trim();
            if (description.isEmpty())
                throw new IllegalArgumentException("description vacío");

            return Mono.just(Movement.builder()
                    .movementId(movementId)
                    .boxId(expectedBoxId)
                    .date(date)
                    .type(type)
                    .amount(amount)
                    .currency(currency)
                    .description(description)
                    .build());

        } catch (Exception e) {
            return Mono.error(new IllegalArgumentException(
                    "Línea inválida («" + line + "»): " + e.getMessage()));
        }
    }

    /** DTO del reporte final. */
    public record UploadReport(long total, long success, long failed) {}
}
