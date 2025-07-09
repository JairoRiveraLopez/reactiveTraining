package co.com.bancolombia.mongo.config;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementDocument {

    @Id
    private String movementId;
    private String boxId;
    private LocalDateTime date;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String description;
}