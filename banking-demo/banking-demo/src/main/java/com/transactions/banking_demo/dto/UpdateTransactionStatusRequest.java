package com.transactions.banking_demo.dto;

import com.transactions.banking_demo.entities.TransactionStatus;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UpdateTransactionStatusRequest {
    @NotNull
    private TransactionStatus status;
}
