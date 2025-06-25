package com.transactions.banking_demo.repository;

import com.transactions.banking_demo.entities.Transaction;
import com.transactions.banking_demo.entities.TransactionStatus;
import com.transactions.banking_demo.entities.TransactionType;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TransactionRepository extends R2dbcRepository<Transaction, UUID> {

    Flux<Transaction> findByStatus(TransactionStatus status);
    Flux<Transaction> findByType(TransactionType type);
    Flux<Transaction> findByStatusAndType(TransactionStatus status, TransactionType type);
}
