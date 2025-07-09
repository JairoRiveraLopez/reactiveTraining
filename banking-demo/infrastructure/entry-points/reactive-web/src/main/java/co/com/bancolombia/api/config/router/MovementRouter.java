package co.com.bancolombia.api.config.router;

import co.com.bancolombia.api.config.handler.MovementUploadHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class MovementRouter {

    @Bean
    public RouterFunction<ServerResponse> movementRoutes(MovementUploadHandler h) {
        return RouterFunctions.route()
                .POST("/api/boxes/{boxId}/movements/upload",
                        r -> r.headers().contentType().orElse(MediaType.MULTIPART_FORM_DATA)
                                .isCompatibleWith(MediaType.MULTIPART_FORM_DATA),
                        h::upload)
                .build();
    }

}
