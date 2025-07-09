package co.com.bancolombia.usecase.getboxbyid;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetBoxByIdUseCase {

    private final BoxRepository boxRepository;
    public Mono<Box> execute(String id){ return boxRepository.findById(id); }

}
