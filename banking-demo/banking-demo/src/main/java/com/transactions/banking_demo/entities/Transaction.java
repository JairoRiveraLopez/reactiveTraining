package com.transactions.banking_demo.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction")
public class Transaction {

    @Id
    private UUID id;
    @Column("amount")
    private BigDecimal amount;
    @Column("currency")
    private String currency;
    @Column("type")
    private TransactionType type;
    @Column("status")
    private TransactionStatus status;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
