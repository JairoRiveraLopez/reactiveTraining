package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class BoxNameUpdatedEvent {
    private final String boxId;
    private final String oldName;
    private final String newName;
    private final Instant occurredOn = Instant.now();
}
