package co.com.bancolombia.usecase.reopenbox;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxReopenedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.NegativeAmountException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ReopenBoxUseCase {

    private final BoxRepository boxRepository;
    private final EventsGateway eventsGateway;

    public Mono<Box> execute(String id) {
        return boxRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Caja no encontrada")))
                .flatMap(box -> {
                    if (box.getCurrentBalance() != null && box.getCurrentBalance().signum() < 0) {
                        return Mono.error(new NegativeAmountException(
                                "No se puede reabrir una caja con saldo negativo"));
                    }
                    return Mono.just(box.reopen());
                })
                .flatMap(boxRepository::save)
                .flatMap(saved ->
                        eventsGateway.emit(new BoxReopenedEvent(id))
                                .thenReturn(saved));
    }
}
