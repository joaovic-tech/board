package tech.joaovic.persistence.dao;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnNameEnum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardColumnDAO {
    private final Connection connection;

    public void insert(final BoardColumnEntity entity) throws SQLException {
        String sql = "INSERT INTO BOARDS_COLUMN (name, nivel, board_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName().name());
            statement.setInt(2, entity.getNivel());
            statement.setLong(3, entity.getBoard().getId());
            statement.executeUpdate();
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public List<BoardColumnEntity> findByBoardId(final Long boardId) throws SQLException {
        String sql = "SELECT id, name, nivel, board_id FROM BOARDS_COLUMN WHERE board_id = ? ORDER BY nivel";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BoardColumnEntity> columns = new ArrayList<>();
                
                while (resultSet.next()) {
                    BoardColumnEntity entity = new BoardColumnEntity();
                    entity.setId(resultSet.getLong("id"));
                    entity.setName(BoardColumnNameEnum.valueOf(resultSet.getString("name")));
                    entity.setNivel(resultSet.getInt("nivel"));
                    entity.getBoard().setId(resultSet.getLong("board_id"));
                    columns.add(entity);
                }
                return columns;
            }
        }
    }

    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        String sql = "SELECT id, name, nivel, board_id FROM BOARDS_COLUMN WHERE id = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BoardColumnEntity entity = new BoardColumnEntity();
                    entity.setId(resultSet.getLong("id"));
                    entity.setName(BoardColumnNameEnum.valueOf(resultSet.getString("name")));
                    entity.setNivel(resultSet.getInt("nivel"));
                    entity.getBoard().setId(resultSet.getLong("board_id"));
                    return Optional.of(entity);
                }
            }
            return Optional.empty();
        }
    }
}
