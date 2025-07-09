package co.com.bancolombia.model.box;
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
public class Box {
    private String id;
    private String name;
    private BoxStatus status;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal currentBalance;

    private static void validateAmount(BigDecimal amount, String campo) {
        if (amount == null || amount.signum() < 0) {
            throw new NegativeAmountException("El campo «" + campo + "» no puede ser negativo");
        }
    }

    public Box addAmount(BigDecimal amount) {
        validateAmount(amount, "amount");
        requireOpen();
        this.currentBalance = this.currentBalance.add(amount);
        return this;
    }

    public Box subtractAmount(BigDecimal amount) {
        validateAmount(amount, "amount");
        requireOpen();
        if (this.currentBalance.compareTo(amount) < 0)
            throw new IllegalArgumentException("Saldo insuficiente");
        this.currentBalance = this.currentBalance.subtract(amount);
        return this;
    }

    public Box close() {
        requireOpen();
        validateAmount(this.currentBalance, "currentBalance");
        this.status = BoxStatus.CLOSED;
        this.closingAmount = this.currentBalance;
        this.closedAt = LocalDateTime.now();
        return this;
    }

    public Box reopen() {
        if (this.status != BoxStatus.CLOSED) {
            throw new IllegalStateException("Solo se pueden reabrir cajas cerradas");
        }
        if (this.currentBalance != null && this.currentBalance.signum() < 0) {
            throw new NegativeAmountException("El saldo actual es negativo; no se puede reabrir");
        }
        this.status        = BoxStatus.OPENED;
        this.openedAt      = LocalDateTime.now();
        this.closingAmount = null;
        this.closedAt      = null;
        this.openingAmount = this.currentBalance;
        return this;
    }

    public Box reopen(BigDecimal openingAmount) {
        validateAmount(openingAmount, "openingAmount");
        if (this.status != BoxStatus.CLOSED) throw new IllegalStateException("Solo cajas cerradas");
        this.status         = BoxStatus.OPENED;
        this.openingAmount  = openingAmount;
        this.currentBalance = openingAmount;
        this.openedAt       = LocalDateTime.now();
        this.closingAmount  = null;
        this.closedAt       = null;
        return this;
    }

    private void requireOpen() {
        if (this.status != BoxStatus.OPENED)
            throw new IllegalStateException("La caja debe estar abierta");
    }
}
