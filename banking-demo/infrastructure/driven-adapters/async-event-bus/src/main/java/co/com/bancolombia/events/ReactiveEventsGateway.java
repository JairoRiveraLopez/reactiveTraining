package co.com.bancolombia.events;

import co.com.bancolombia.model.events.*;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.reactivecommons.api.domain.DomainEvent;
import org.reactivecommons.api.domain.DomainEventBus;
import org.reactivecommons.async.impl.config.annotations.EnableDomainEventBus;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Log
@RequiredArgsConstructor
@EnableDomainEventBus
public class ReactiveEventsGateway implements EventsGateway {

    private static final Map<Class<?>, String> ROUTING = Map.of(
            BoxCreatedEvent.class       , "box.event.created",
            BoxNameUpdatedEvent.class   , "box.event.name.updated",
            BoxDeletedEvent.class       , "box.event.deleted",
            BoxClosedEvent.class        , "box.event.closed",
            BoxOpenedEvent.class        , "box.event.opened",
            BoxReopenedEvent.class      , "box.event.reopened"
    );

    private final DomainEventBus domainEventBus;

    @Override
    public Mono<Void> emit(Object event) {

        String routingKey = ROUTING.getOrDefault(
                event.getClass(),
                "box.event." + toDotCase(event.getClass().getSimpleName().replace("Event", ""))
        );

        DomainEvent<Object> message =
                new DomainEvent<>(routingKey, UUID.randomUUID().toString(), event);

        return Mono.from(domainEventBus.emit(message))
                .doOnSubscribe(s -> log.fine(() ->
                        String.format("Emitiendo evento [%s]", routingKey)))
                .doOnError(e -> log.severe(String.format(
                        "Fallo al emitir [%s]: %s", routingKey, e.getMessage())));
    }

    private static String toDotCase(String text) {
        return text.replaceAll("([a-z])([A-Z])", "$1.$2")
                .toLowerCase();
    }
}
