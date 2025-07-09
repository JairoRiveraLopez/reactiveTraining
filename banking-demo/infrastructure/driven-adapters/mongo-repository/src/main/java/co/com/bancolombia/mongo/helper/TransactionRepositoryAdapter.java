package co.com.bancolombia.mongo.helper;

import co.com.bancolombia.model.transaction.Transaction;
import co.com.bancolombia.model.transaction.gateways.TransactionRepository;
import co.com.bancolombia.mongo.config.TransactionReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionReactiveRepository transactionReactiveRepository;

    @Override
    public Mono<Transaction> findById(String id) {
        return transactionReactiveRepository.findById(id)
                .map(TransactionMapper::toEntity);
    }

    @Override
    public Flux<Transaction> findAll() {
        return transactionReactiveRepository.findAll()
                .map(TransactionMapper::toEntity);
    }

    @Override
    public Flux<Transaction> findByBoxId(String boxId) {
        return transactionReactiveRepository.findByBoxId(boxId)
                .map(TransactionMapper::toEntity);
    }

    @Override
    public Mono<Transaction> save(Transaction tx) {
        return transactionReactiveRepository.save(TransactionMapper.toDocument(tx))
                .map(TransactionMapper::toEntity);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return transactionReactiveRepository.deleteById(id);
    }

}