package co.com.bancolombia.mongo.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "boxes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BoxDocument {
    @Id
    private String id;
    private String name;
    private String status;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal currentBalance;
}
