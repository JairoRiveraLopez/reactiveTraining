package co.com.bancolombia.usecase.listallboxes;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor

public class ListAllBoxesUseCase {

    private final BoxRepository boxRepository;
    public Flux<Box> execute(){ return boxRepository.findAll(); }

}
