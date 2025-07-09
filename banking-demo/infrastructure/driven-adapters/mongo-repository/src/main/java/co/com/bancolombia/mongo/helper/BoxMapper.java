package co.com.bancolombia.mongo.helper;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.BoxStatus;
import co.com.bancolombia.mongo.config.BoxDocument;

public class BoxMapper {
    public static BoxDocument toDocument(Box box){
        return BoxDocument.builder()
                .id(box.getId())
                .name(box.getName())
                .status(box.getStatus().name())
                .openingAmount(box.getOpeningAmount())
                .closingAmount(box.getClosingAmount())
                .openedAt(box.getOpenedAt())
                .closedAt(box.getClosedAt())
                .currentBalance(box.getCurrentBalance())
                .build();
    }
    public static Box toEntity(BoxDocument boxDocument){
        return Box.builder()
                .id(boxDocument.getId())
                .name(boxDocument.getName())
                .status(BoxStatus.valueOf(boxDocument.getStatus()))
                .openingAmount(boxDocument.getOpeningAmount())
                .closingAmount(boxDocument.getClosingAmount())
                .openedAt(boxDocument.getOpenedAt())
                .closedAt(boxDocument.getClosedAt())
                .currentBalance(boxDocument.getCurrentBalance())
                .build();
    }
}
