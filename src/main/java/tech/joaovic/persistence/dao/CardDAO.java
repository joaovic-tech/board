package tech.joaovic.persistence.dao;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.entity.CardEntity;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnTypeEnum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class CardDAO {
    private final Connection connection;

    public void insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) values (?, ?, ?);";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setLong(3, entity.getBoardColumn().getId());
            statement.executeUpdate();
            
            try (var resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    entity.setId(resultSet.getLong(1));
                }
            }
        }
    }
    
    public int countCardsByBoardId(final Long boardId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CARDS c INNER JOIN BOARDS_COLUMN bc ON c.board_column_id = bc.id WHERE bc.board_id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }
    
    public List<CardEntity> findCardsByBoardId(final Long boardId) throws SQLException {
        String sql = "SELECT c.id, c.title, c.description, c.created_at, c.moved_at, c.status, " +
                    "bc.id as column_id, bc.type, bc.custom_name, bc.nivel " +
                    "FROM CARDS c " +
                    "INNER JOIN BOARDS_COLUMN bc ON c.board_column_id = bc.id " +
                    "WHERE bc.board_id = ? " +
                    "ORDER BY bc.nivel, c.created_at";
        
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CardEntity> cards = new ArrayList<>();
                
                while (resultSet.next()) {
                    CardEntity card = new CardEntity();
                    card.setId(resultSet.getLong("id"));
                    card.setTitle(resultSet.getString("title"));
                    card.setDescription(resultSet.getString("description"));
                    card.setCreatedAt(resultSet.getTimestamp("created_at") != null 
                        ? resultSet.getTimestamp("created_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    card.setMovedAt(resultSet.getTimestamp("moved_at") != null 
                        ? resultSet.getTimestamp("moved_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    card.setStatus(resultSet.getString("status"));
                    
                    // Preencher informações da coluna
                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(resultSet.getLong("column_id"));
                    column.setType(BoardColumnTypeEnum.valueOf(resultSet.getString("type")));
                    column.setName(resultSet.getString("custom_name"));
                    column.setNivel(resultSet.getInt("nivel"));
                    
                    card.setBoardColumn(column);
                    cards.add(card);
                }
                return cards;
            }
        }
    }
    
    public Optional<CardEntity> findById(final Long cardId) throws SQLException {
        String sql = "SELECT c.id, c.title, c.description, c.created_at, c.moved_at, c.status, " +
                    "bc.id as column_id, bc.type, bc.custom_name, bc.nivel, bc.board_id " +
                    "FROM CARDS c " +
                    "INNER JOIN BOARDS_COLUMN bc ON c.board_column_id = bc.id " +
                    "WHERE c.id = ?";
        
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CardEntity card = new CardEntity();
                    card.setId(resultSet.getLong("id"));
                    card.setTitle(resultSet.getString("title"));
                    card.setDescription(resultSet.getString("description"));
                    card.setCreatedAt(resultSet.getTimestamp("created_at") != null 
                        ? resultSet.getTimestamp("created_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    card.setMovedAt(resultSet.getTimestamp("moved_at") != null 
                        ? resultSet.getTimestamp("moved_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) 
                        : null);
                    card.setStatus(resultSet.getString("status"));
                    
                    // Preencher informações da coluna
                    BoardColumnEntity column = new BoardColumnEntity();
                    column.setId(resultSet.getLong("column_id"));
                    column.setType(BoardColumnTypeEnum.valueOf(resultSet.getString("type")));
                    column.setName(resultSet.getString("custom_name"));
                    column.setNivel(resultSet.getInt("nivel"));
                    column.getBoard().setId(resultSet.getLong("board_id"));
                    
                    card.setBoardColumn(column);
                    return Optional.of(card);
                }
                return Optional.empty();
            }
        }
    }
    
    public void updateColumn(final Long cardId, final Long newColumnId) throws SQLException {
        String sql = "UPDATE CARDS SET board_column_id = ?, moved_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, newColumnId);
            statement.setLong(2, cardId);
            statement.executeUpdate();
        }
    }
}
