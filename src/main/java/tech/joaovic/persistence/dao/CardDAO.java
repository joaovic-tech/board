package tech.joaovic.persistence.dao;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@AllArgsConstructor
public class CardDAO {
    private final Connection connection;

    public void insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id, board_id) values (?, ?, ?, ?);";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setLong(3, entity.getBoardColumn().getId());
            statement.setLong(4, entity.getBoardColumn().getBoard().getId());
            statement.executeUpdate();
            
            try (var resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    entity.setId(resultSet.getLong(1));
                }
            }
        }
    }
    
    public int countCardsByBoardId(final Long boardId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CARDS WHERE board_id = ?";
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
}
