package co.com.bancolombia.model.transaction.gateways;

import co.com.bancolombia.model.transaction.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository {
    Mono<Transaction> findById(String id);
    Flux<Transaction> findAll();
    Flux<Transaction> findByBoxId(String boxId);
    Mono<Transaction> save(Transaction transaction);
    Mono<Void> deleteById(String id);
}
