package co.com.bancolombia.usecase.closebox;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxClosedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CloseBoxUseCase {

    private final BoxRepository boxRepository;
    private final EventsGateway eventsGateway;

    public Mono<Box> execute(String id) {
        return boxRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Caja no encontrada")))
                .map(Box::close)
                .flatMap(boxRepository::save)
                .flatMap(saved ->
                        eventsGateway.emit(
                                new BoxClosedEvent(saved.getId(), saved.getClosingAmount())
                        ).thenReturn(saved)
                );
    }

}
