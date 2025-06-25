package com.transactions.banking_demo.services;

import com.transactions.banking_demo.dto.CreateTransactionRequest;
import com.transactions.banking_demo.dto.LedgerRequest;
import com.transactions.banking_demo.dto.UpdateTransactionStatusRequest;
import com.transactions.banking_demo.entities.Transaction;
import com.transactions.banking_demo.entities.TransactionStatus;
import com.transactions.banking_demo.entities.TransactionType;
import com.transactions.banking_demo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerService       ledgerService;

    /* ------------------------------------------------------------------ */
    /*  MÉTODOS CRUD REACTIVOS                                            */
    /* ------------------------------------------------------------------ */

    /** Alta de transacción + post-proceso en Ledger */
    public Mono<Transaction> createTransaction(CreateTransactionRequest req) {

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setAmount(req.getAmount());
        tx.setCurrency(req.getCurrency());
        tx.setType(req.getType());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCreatedAt(Instant.now());

        return transactionRepository.save(tx)
                .doOnSuccess(t -> log.info("Transaction created: {}", t.getId()))
                .flatMap(this::processWithLedger);
    }

    /** Consulta por ID (UUID) */
    public Mono<Transaction> getTransactionById(String id) {
        return transactionRepository.findById(UUID.fromString(id))
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + id)))
                .doOnNext(t -> log.info("Retrieved: {}", t.getId()));
    }

    /** Listado completo */
    public Flux<Transaction> getAllTransactions() {
        return transactionRepository.findAll()
                .doOnNext(t -> log.debug("Found: {}", t.getId()));
    }

    /** Filtro por estado */
    public Flux<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status)
                .doOnNext(t -> log.debug("Found by status {}: {}", status, t.getId()));
    }

    /** Filtro por tipo */
    public Flux<Transaction> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByType(type)
                .doOnNext(t -> log.debug("Found by type {}: {}", type, t.getId()));
    }

    /** Cambio de estado */
    public Mono<Transaction> updateTransactionStatus(UUID id, UpdateTransactionStatusRequest req) {
        return transactionRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + id)))
                .flatMap(tx -> {
                    tx.setStatus(req.getStatus());
                    return transactionRepository.save(tx);
                })
                .doOnSuccess(t -> log.info("Updated {} → {}", id, req.getStatus()));
    }

    public Mono<Void> deleteTransaction(UUID id) {
        return transactionRepository.existsById(id)
                .flatMap(exists -> exists
                        ? transactionRepository.deleteById(id)
                        : Mono.error(new RuntimeException("Transaction not found: " + id)))
                .doOnSuccess(v -> log.info("Deleted: {}", id));
    }

    /* ------------------------------------------------------------------ */
    /*  Llamada a Ledger (idéntica a la versión Mongo)                    */
    /* ------------------------------------------------------------------ */

    private Mono<Transaction> processWithLedger(Transaction tx) {
        LedgerRequest lr = new LedgerRequest(tx.getId(), tx.getAmount(),
                tx.getCurrency(), tx.getType());

        return ledgerService.processTransaction(lr)
                .flatMap(res -> {
                    tx.setStatus(res.isSuccess()
                            ? TransactionStatus.POSTED
                            : TransactionStatus.FAILED);
                    return transactionRepository.save(tx);
                })
                .doOnSuccess(t -> log.info("Ledger processed {}", t.getId()))
                .onErrorResume(ex -> {
                    log.error("Ledger error {} → {}", tx.getId(), ex.getMessage());
                    tx.setStatus(TransactionStatus.FAILED);
                    return transactionRepository.save(tx);
                });
    }
}
