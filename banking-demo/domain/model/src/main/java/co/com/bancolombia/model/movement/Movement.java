package co.com.bancolombia.model.movement;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Movement {

    private String movementId;
    private String boxId;
    private LocalDateTime date;
    private MovementType type;
    private BigDecimal amount;
    private String currency;
    private String description;
}