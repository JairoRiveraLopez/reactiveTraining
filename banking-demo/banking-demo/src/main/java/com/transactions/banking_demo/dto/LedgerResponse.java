package com.transactions.banking_demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerResponse {
    private UUID transactionId;
    private boolean success;
    private String message;
}
