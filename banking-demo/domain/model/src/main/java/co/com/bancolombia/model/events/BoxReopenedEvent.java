package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class BoxReopenedEvent {
    private final String boxId;
    private final Instant occurredOn = Instant.now();
}
