package co.com.bancolombia.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@AllArgsConstructor
@Getter
public class BoxMovementsUploadedEvent {
    private final String boxId;
    private final long   total;
    private final long   success;
    private final long   failed;
    private final String uploadedBy;
    private final Instant occurredOn = Instant.now();
}