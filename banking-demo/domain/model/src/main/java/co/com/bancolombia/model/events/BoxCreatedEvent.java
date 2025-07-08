package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class BoxCreatedEvent {
    private final String boxId;
    private final String name;
    private final Instant occurredOn = Instant.now();
}
