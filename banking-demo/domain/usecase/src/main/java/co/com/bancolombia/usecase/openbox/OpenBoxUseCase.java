package co.com.bancolombia.usecase.openbox;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.BoxStatus;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxOpenedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class OpenBoxUseCase {

    private final BoxRepository boxRepository;
    private final EventsGateway eventsGateway;

    public Mono<Box> execute(String id, BigDecimal openingAmount) {
        return boxRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Caja no encontrada")))
                .flatMap(box -> {
                    if (box.getStatus() == BoxStatus.OPENED) {
                        return Mono.error(new IllegalStateException("La caja ya está abierta"));
                    }
                    box.setStatus(BoxStatus.OPENED);
                    box.setOpeningAmount(openingAmount);
                    box.setCurrentBalance(openingAmount);
                    box.setOpenedAt(LocalDateTime.now());
                    box.setClosingAmount(null);
                    box.setClosedAt(null);
                    return boxRepository.save(box);
                })
                .flatMap(saved ->
                        eventsGateway.emit(
                                new BoxOpenedEvent(saved.getId(), saved.getOpeningAmount())
                        ).thenReturn(saved)
                );
    }

}
