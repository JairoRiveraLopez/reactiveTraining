package co.com.bancolombia.events.handlers;

import lombok.extern.java.Log;
import org.reactivecommons.api.domain.DomainEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Log
@Component
public class BoxEventsHandler {

    public Mono<Void> handle(DomainEvent<Object> event) {
        log.info(() -> "📥 Box event → " + event.getName() + " : " + event.getData());
        return Mono.empty();
    }

    public Mono<Void> handleBroadcast(DomainEvent<Object> event) {
        log.info(() -> "📢 Broadcast → " + event.getName());
        return Mono.empty();
    }
}