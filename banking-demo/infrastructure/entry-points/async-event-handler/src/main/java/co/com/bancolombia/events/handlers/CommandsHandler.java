package co.com.bancolombia.events.handlers;


import org.reactivecommons.api.domain.Command;
import org.reactivecommons.async.impl.config.annotations.EnableCommandListeners;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import lombok.extern.java.Log;

@Log
@Component
@EnableCommandListeners
public class CommandsHandler {

    public Mono<Void> handleCommand(Command<Object> command) {
        log.info(() -> "📨 Command → " + command.getName() + " : " + command.getData());
        return Mono.empty();
    }

}
