package tech.joaovic.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BlockEntity {
    private Long id;
    private String blockReason;
    private OffsetDateTime blockedAt;
    private String unblockedReason;
    private OffsetDateTime unblockAt;

}
