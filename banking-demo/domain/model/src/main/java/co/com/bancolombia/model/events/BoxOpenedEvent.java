package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@Getter
public class BoxOpenedEvent {

    private final String boxId;
    private final BigDecimal openingAmount;
    private final Instant occurredOn = Instant.now();

}
