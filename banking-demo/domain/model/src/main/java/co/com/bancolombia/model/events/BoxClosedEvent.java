package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@Getter
public class BoxClosedEvent {
    private final String  boxId;
    private final BigDecimal closingAmount;
    private final Instant occurredOn = Instant.now();
}