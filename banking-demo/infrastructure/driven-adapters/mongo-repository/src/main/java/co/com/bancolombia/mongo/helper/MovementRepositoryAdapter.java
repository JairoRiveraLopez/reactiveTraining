package co.com.bancolombia.mongo.helper;

import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.movement.gateways.MovementRepository;
import co.com.bancolombia.mongo.config.MovementReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MovementRepositoryAdapter implements MovementRepository {

    private final MovementReactiveRepository repo;

    @Override
    public Mono<Movement> save(Movement movement) {
        return repo.save(MovementMapper.toDocument(movement))
                .map(MovementMapper::toEntity);
    }

    @Override
    public Flux<Movement> saveAll(Flux<Movement> movements) {
        return repo.saveAll(movements.map(MovementMapper::toDocument))
                .map(MovementMapper::toEntity);
    }
}
