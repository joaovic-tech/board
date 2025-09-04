package tech.joaovic.persistence.dao;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.entity.CardMovementEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CardMovementDAO {
    private final Connection connection;

    public void insert(final CardMovementEntity entity) throws SQLException {
        String sql = "INSERT INTO CARD_MOVEMENTS (card_id, from_column_id, to_column_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, entity.getCard().getId());
            if (entity.getFromColumn() != null) {
                statement.setLong(2, entity.getFromColumn().getId());
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }
            statement.setLong(3, entity.getToColumn().getId());
            statement.executeUpdate();
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public List<CardMovementEntity> findByCardId(final Long cardId) throws SQLException {
        String sql = "SELECT cm.id, cm.card_id, cm.moved_at, " +
                    "from_col.id as from_col_id, from_col.custom_name as from_col_name, from_col.type as from_col_type, " +
                    "to_col.id as to_col_id, to_col.custom_name as to_col_name, to_col.type as to_col_type " +
                    "FROM CARD_MOVEMENTS cm " +
                    "LEFT JOIN BOARDS_COLUMN from_col ON cm.from_column_id = from_col.id " +
                    "INNER JOIN BOARDS_COLUMN to_col ON cm.to_column_id = to_col.id " +
                    "WHERE cm.card_id = ? " +
                    "ORDER BY cm.moved_at";
        
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CardMovementEntity> movements = new ArrayList<>();
                
                while (resultSet.next()) {
                    CardMovementEntity movement = new CardMovementEntity();
                    movement.setId(resultSet.getLong("id"));
                    movement.getCard().setId(resultSet.getLong("card_id"));
                    movement.setMovedAt(resultSet.getTimestamp("moved_at") != null 
                        ? resultSet.getTimestamp("moved_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    
                    // From column (pode ser null para primeiro movimento)
                    Long fromColId = resultSet.getLong("from_col_id");
                    if (fromColId != 0 && !resultSet.wasNull()) {
                        movement.setFromColumn(new tech.joaovic.persistence.entity.BoardColumnEntity());
                        movement.getFromColumn().setId(fromColId);
                        movement.getFromColumn().setName(resultSet.getString("from_col_name"));
                        movement.getFromColumn().setType(tech.joaovic.persistence.entity.BoardColumnTypeEnum.valueOf(resultSet.getString("from_col_type")));
                    } else {
                        movement.setFromColumn(null); // Explicitamente definir como null
                    }
                    
                    // To column
                    movement.getToColumn().setId(resultSet.getLong("to_col_id"));
                    movement.getToColumn().setName(resultSet.getString("to_col_name"));
                    movement.getToColumn().setType(tech.joaovic.persistence.entity.BoardColumnTypeEnum.valueOf(resultSet.getString("to_col_type")));
                    
                    movements.add(movement);
                }
                return movements;
            }
        }
    }
    
    public List<CardMovementEntity> findByBoardId(final Long boardId) throws SQLException {
        String sql = "SELECT cm.id, cm.card_id, cm.moved_at, " +
                    "c.title as card_title, " +
                    "from_col.id as from_col_id, from_col.custom_name as from_col_name, from_col.type as from_col_type, " +
                    "to_col.id as to_col_id, to_col.custom_name as to_col_name, to_col.type as to_col_type " +
                    "FROM CARD_MOVEMENTS cm " +
                    "INNER JOIN CARDS c ON cm.card_id = c.id " +
                    "LEFT JOIN BOARDS_COLUMN from_col ON cm.from_column_id = from_col.id " +
                    "INNER JOIN BOARDS_COLUMN to_col ON cm.to_column_id = to_col.id " +
                    "WHERE to_col.board_id = ? " +
                    "ORDER BY cm.card_id, cm.moved_at";
        
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CardMovementEntity> movements = new ArrayList<>();
                
                while (resultSet.next()) {
                    CardMovementEntity movement = new CardMovementEntity();
                    movement.setId(resultSet.getLong("id"));
                    movement.getCard().setId(resultSet.getLong("card_id"));
                    movement.getCard().setTitle(resultSet.getString("card_title"));
                    movement.setMovedAt(resultSet.getTimestamp("moved_at") != null 
                        ? resultSet.getTimestamp("moved_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    
                    // From column (pode ser null para primeiro movimento)
                    Long fromColId = resultSet.getLong("from_col_id");
                    if (fromColId != 0 && !resultSet.wasNull()) {
                        movement.setFromColumn(new tech.joaovic.persistence.entity.BoardColumnEntity());
                        movement.getFromColumn().setId(fromColId);
                        movement.getFromColumn().setName(resultSet.getString("from_col_name"));
                        movement.getFromColumn().setType(tech.joaovic.persistence.entity.BoardColumnTypeEnum.valueOf(resultSet.getString("from_col_type")));
                    } else {
                        movement.setFromColumn(null); // Explicitamente definir como null
                    }
                    
                    // To column
                    movement.getToColumn().setId(resultSet.getLong("to_col_id"));
                    movement.getToColumn().setName(resultSet.getString("to_col_name"));
                    movement.getToColumn().setType(tech.joaovic.persistence.entity.BoardColumnTypeEnum.valueOf(resultSet.getString("to_col_type")));
                    
                    movements.add(movement);
                }
                return movements;
            }
        }
    }
}
