package com.transactions.banking_demo.handlers;

import com.transactions.banking_demo.entities.Transaction;
import com.transactions.banking_demo.entities.TransactionStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LedgerConsumer {

    @RabbitListener(queues = "ledger.entry.request.queue")
    public Transaction receive(Transaction tx) {
        tx.setStatus(TransactionStatus.POSTED);
        tx.setCreatedAt(Instant.now());
        return tx;
    }

}
