package co.com.bancolombia.mongo.config;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MovementReactiveRepository
        extends ReactiveMongoRepository<MovementDocument, String> {}
