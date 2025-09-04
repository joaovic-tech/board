package tech.joaovic.persistence.dao;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.entity.BlockEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@AllArgsConstructor
public class BlockDAO {
    private final Connection connection;

    public void insert(final BlockEntity entity) throws SQLException {
        String sql = "INSERT INTO BLOCKS (block_reason, card_id) VALUES (?, ?)";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getBlockReason());
            statement.setLong(2, entity.getCard().getId());
            statement.executeUpdate();
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public void updateUnblock(final Long blockId, final String unblockReason) throws SQLException {
        String sql = "UPDATE BLOCKS SET unblocked_reason = ?, unblock_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, unblockReason);
            statement.setLong(2, blockId);
            statement.executeUpdate();
        }
    }
    
    public Optional<BlockEntity> findActiveBlockByCardId(final Long cardId) throws SQLException {
        String sql = "SELECT id, block_reason, blocked_at, unblocked_reason, unblock_at, card_id " +
                    "FROM BLOCKS WHERE card_id = ? AND unblock_at IS NULL " +
                    "ORDER BY blocked_at DESC LIMIT 1";
        
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BlockEntity block = new BlockEntity();
                    block.setId(resultSet.getLong("id"));
                    block.setBlockReason(resultSet.getString("block_reason"));
                    block.setBlockedAt(resultSet.getTimestamp("blocked_at") != null 
                        ? resultSet.getTimestamp("blocked_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    block.setUnblockedReason(resultSet.getString("unblocked_reason"));
                    block.setUnblockAt(resultSet.getTimestamp("unblock_at") != null 
                        ? resultSet.getTimestamp("unblock_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    
                    // Definir ID do card no relacionamento
                    block.getCard().setId(resultSet.getLong("card_id"));
                    
                    return Optional.of(block);
                }
                return Optional.empty();
            }
        }
    }
}
