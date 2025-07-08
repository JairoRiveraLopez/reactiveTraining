package co.com.bancolombia.api.config.router;

import co.com.bancolombia.api.config.handler.BoxHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BoxRouter {

    @Bean
    public RouterFunction<ServerResponse> boxRoutes(BoxHandler h) {
        return RouterFunctions.route()
                .path("/boxes", builder -> builder
                        .POST(""        , h::create)
                        .GET (""        , h::findAll)
                        .GET ("/{id}"   , h::findById)
                        .DELETE("/{id}" , h::delete)

                        .PATCH("/{id}/name"  , h::updateName)
                        .PATCH("/{id}/open"  , h::open)
                        .PATCH("/{id}/close" , h::close)
                        .PATCH("/{id}/reopen", h::reopen)
                )
                .build();
    }
}