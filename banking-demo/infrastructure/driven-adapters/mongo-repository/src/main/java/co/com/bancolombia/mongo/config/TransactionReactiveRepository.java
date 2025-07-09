package co.com.bancolombia.mongo.config;

import co.com.bancolombia.mongo.config.TransactionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionReactiveRepository
        extends ReactiveMongoRepository<TransactionDocument, String> {

    Flux<TransactionDocument> findByBoxId(String boxId);
}
