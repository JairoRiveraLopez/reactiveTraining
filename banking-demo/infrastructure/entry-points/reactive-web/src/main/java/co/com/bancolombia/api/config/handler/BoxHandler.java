package co.com.bancolombia.api.config.handler;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.usecase.closebox.CloseBoxUseCase;
import co.com.bancolombia.usecase.createbox.CreateBoxUseCase;
import co.com.bancolombia.usecase.deletebox.DeleteBoxUseCase;
import co.com.bancolombia.usecase.getboxbyid.GetBoxByIdUseCase;
import co.com.bancolombia.usecase.listallboxes.ListAllBoxesUseCase;
import co.com.bancolombia.usecase.openbox.OpenBoxUseCase;
import co.com.bancolombia.usecase.reopenbox.ReopenBoxUseCase;
import co.com.bancolombia.usecase.updateboxname.UpdateBoxNameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BoxHandler {

    private final CreateBoxUseCase createBox;
    private final GetBoxByIdUseCase getById;
    private final ListAllBoxesUseCase listAll;
    private final UpdateBoxNameUseCase updateName;
    private final DeleteBoxUseCase deleteBox;
    private final OpenBoxUseCase openBox;
    private final CloseBoxUseCase closeBox;
    private final ReopenBoxUseCase reopenBox;


    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(CreateBoxRequest.class)
                .flatMap(dto -> createBox.execute(dto.name(), dto.openingAmount()))
                .flatMap(box -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(box));
    }

    public Mono<ServerResponse> findById(ServerRequest req) {
        return getById.execute(req.pathVariable("id"))
                .flatMap(box -> ServerResponse.ok().bodyValue(box))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findAll(ServerRequest req) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(listAll.execute(), Box.class);
    }

    public Mono<ServerResponse> updateName(ServerRequest req) {
        return req.bodyToMono(UpdateNameRequest.class)
                .flatMap(dto ->
                        updateName.execute(req.pathVariable("id"), dto.newName()))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        return deleteBox.execute(req.pathVariable("id"))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> open(ServerRequest req) {
        return req.bodyToMono(OpenRequest.class)
                .flatMap(dto ->
                        openBox.execute(req.pathVariable("id"), dto.openingAmount()))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> close(ServerRequest req) {
        return closeBox.execute(req.pathVariable("id"))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> reopen(ServerRequest req) {
        return reopenBox.execute(req.pathVariable("id"))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public record CreateBoxRequest(String name, BigDecimal openingAmount) {}
    public record UpdateNameRequest(String newName) {}
    public record OpenRequest(BigDecimal openingAmount) {}
}
