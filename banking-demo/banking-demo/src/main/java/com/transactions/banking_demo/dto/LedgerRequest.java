package com.transactions.banking_demo.dto;

import com.transactions.banking_demo.entities.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerRequest {
    private UUID transactionId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
}
