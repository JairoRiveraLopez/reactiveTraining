package co.com.bancolombia.usecase.deletebox;

import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxDeletedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor

public class DeleteBoxUseCase {

    private final BoxRepository boxRepository;
    private final EventsGateway gateway;

    public Mono<Void> execute(String id){
        return boxRepository.softDelete(id)
                .then(gateway.emit(new BoxDeletedEvent(id)));
    }

}
