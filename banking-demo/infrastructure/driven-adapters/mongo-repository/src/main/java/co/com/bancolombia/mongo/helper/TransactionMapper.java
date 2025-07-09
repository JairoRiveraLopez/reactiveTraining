package co.com.bancolombia.mongo.helper;

import co.com.bancolombia.model.transaction.Transaction;
import co.com.bancolombia.model.transaction.TransactionType;
import co.com.bancolombia.mongo.config.TransactionDocument;

public final class TransactionMapper {

    private TransactionMapper() { }

    public static TransactionDocument toDocument(Transaction transaction) {
        return TransactionDocument.builder()
                .id(transaction.getId())
                .boxId(transaction.getBoxId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public static Transaction toEntity(TransactionDocument transactionDocument) {
        return Transaction.builder()
                .id(transactionDocument.getId())
                .boxId(transactionDocument.getBoxId())
                .type(TransactionType.valueOf(transactionDocument.getType()))
                .amount(transactionDocument.getAmount())
                .description(transactionDocument.getDescription())
                .createdAt(transactionDocument.getCreatedAt())
                .build();
    }
}
