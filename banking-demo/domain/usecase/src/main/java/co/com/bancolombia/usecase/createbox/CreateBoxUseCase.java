package co.com.bancolombia.usecase.createbox;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.BoxStatus;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.BoxCreatedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.NegativeAmountException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateBoxUseCase {

    private final BoxRepository     boxRepository;
    private final EventsGateway     eventsGateway;

    public Mono<Box> execute(String name, BigDecimal openingAmount) {

        if (openingAmount == null || openingAmount.signum() < 0) {
            return Mono.error(new NegativeAmountException(
                    "El monto de apertura no puede ser negativo"));
        }

        Box box = Box.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .status(BoxStatus.OPENED)
                .openingAmount(openingAmount)
                .currentBalance(openingAmount)
                .openedAt(LocalDateTime.now())
                .build();

        return boxRepository.save(box)
                .flatMap(saved -> eventsGateway
                        .emit(new BoxCreatedEvent(saved.getId(), saved.getName()))
                        .thenReturn(saved));
    }
}
