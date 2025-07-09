package co.com.bancolombia.mongo.helper;

import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.movement.MovementType;
import co.com.bancolombia.mongo.config.MovementDocument;

public final class MovementMapper {

    private MovementMapper() {}

    public static MovementDocument toDocument(Movement m) {
        return MovementDocument.builder()
                .movementId(m.getMovementId())
                .boxId(m.getBoxId())
                .date(m.getDate())
                .type(m.getType().name())
                .amount(m.getAmount())
                .currency(m.getCurrency())
                .description(m.getDescription())
                .build();
    }

    public static Movement toEntity(MovementDocument d) {
        return Movement.builder()
                .movementId(d.getMovementId())
                .boxId(d.getBoxId())
                .date(d.getDate())
                .type(MovementType.valueOf(d.getType()))
                .amount(d.getAmount())
                .currency(d.getCurrency())
                .description(d.getDescription())
                .build();
    }
}