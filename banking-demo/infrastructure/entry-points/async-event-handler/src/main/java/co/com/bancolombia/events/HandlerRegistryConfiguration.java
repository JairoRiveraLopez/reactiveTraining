package co.com.bancolombia.events;
import co.com.bancolombia.events.handlers.BoxEventsHandler;
import co.com.bancolombia.events.handlers.CommandsHandler;
import co.com.bancolombia.events.handlers.QueriesHandler;
import org.reactivecommons.async.api.HandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerRegistryConfiguration {

    private static final String SUBSCRIPTION_ID = "box-events";
    private static final String COMMAND_PATTERN = "box.command.*";
    private static final String QUERY_PATTERN   = "box.query.*";

    @Bean
    public HandlerRegistry handlerRegistry(CommandsHandler commands,
                                           BoxEventsHandler events,
                                           QueriesHandler queries) {

        return HandlerRegistry.register()
                .listenEvent(SUBSCRIPTION_ID, events::handle, Object.class)
                .listenNotificationEvent("box.notification.*",
                        events::handleBroadcast,
                        Object.class)
                .handleCommand(COMMAND_PATTERN, commands::handleCommand, Object.class)
                .serveQuery(QUERY_PATTERN,     queries::handleQuery,    Object.class);
    }
}