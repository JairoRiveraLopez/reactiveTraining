package co.com.bancolombia.events.handlers;


import org.reactivecommons.async.impl.config.annotations.EnableQueryListeners;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import lombok.extern.java.Log;

@Log
@Component
@EnableQueryListeners
public class QueriesHandler {

    public Mono<Object> handleQuery(Object query) {
        log.info(() -> " Query → " + query);
        return Mono.just("dummy-response");
    }
}
