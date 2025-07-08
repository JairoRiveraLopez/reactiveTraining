package co.com.bancolombia.usecase.updateboxname;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxNameUpdatedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateBoxNameUseCase {

    private final BoxRepository boxRepository;
    private final EventsGateway gateway;

    public Mono<Box> execute(String id, String newName){
        return boxRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Caja no encontrada")))
                .flatMap(box -> {
                    String old = box.getName();
                    box.setName(newName);
                    return boxRepository.save(box)
                            .flatMap(saved -> gateway.emit(new BoxNameUpdatedEvent(id, old, newName))
                                    .thenReturn(saved));
                });
    }

}
