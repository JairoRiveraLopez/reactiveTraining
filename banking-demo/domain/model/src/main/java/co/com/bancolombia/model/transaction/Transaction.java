package co.com.bancolombia.model.transaction;
import co.com.bancolombia.model.exception.NegativeAmountException;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Transaction {
    private String id;
    private String boxId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public void validate() {
        if (amount == null || amount.signum() < 0) {
            throw new NegativeAmountException("El monto de la transacción no puede ser negativo");
        }
    }
}
