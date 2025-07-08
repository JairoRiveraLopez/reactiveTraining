package co.com.bancolombia.mongo.helper;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.mongo.config.BoxReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BoxRepositoryAdapter implements BoxRepository {

    private final BoxReactiveRepository repository;

    @Override
    public Mono<Box> findById(String id) {
        return repository.findById(id).map(BoxMapper::toEntity);
    }

    @Override
    public Flux<Box> findAll() {
        return repository.findAll().map(BoxMapper::toEntity);
    }

    @Override
    public Mono<Box> save(Box box) {
        return repository.save(BoxMapper.toDocument(box))
                .map(BoxMapper::toEntity);
    }

    @Override
    public Mono<Void> softDelete(String id) {
        return repository.findById(id)
                .flatMap(doc -> {
                    doc.setStatus("DELETED");
                    return repository.save(doc).then();
                });
    }

}
