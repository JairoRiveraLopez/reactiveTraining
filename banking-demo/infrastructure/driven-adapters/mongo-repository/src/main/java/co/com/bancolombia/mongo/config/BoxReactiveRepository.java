package co.com.bancolombia.mongo.config;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BoxReactiveRepository extends ReactiveMongoRepository<BoxDocument, String> {}