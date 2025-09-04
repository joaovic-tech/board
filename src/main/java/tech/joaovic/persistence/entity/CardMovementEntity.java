package tech.joaovic.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CardMovementEntity {
    private Long id;
    private CardEntity card = new CardEntity();
    private BoardColumnEntity fromColumn;
    private BoardColumnEntity toColumn = new BoardColumnEntity();
    private OffsetDateTime movedAt;
}
