package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.BoardColumnDAO;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnNameEnum;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardColumnService {
    private final Connection connection;

    public BoardColumnEntity create(final BoardColumnEntity entity) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        try {
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public void createWithoutCommit(final BoardColumnEntity entity) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        dao.insert(entity);
    }
    
    public boolean globalColumnsExist() throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        Long firstBoardId = getFirstBoardId();
        if (firstBoardId == null) return false;
        
        List<BoardColumnEntity> columns = dao.findByBoardId(firstBoardId);
        return columns.size() >= 5;
    }
    
    public BoardColumnEntity findInitialColumn() throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        Long firstBoardId = getFirstBoardId();
        List<BoardColumnEntity> columns = dao.findByBoardId(firstBoardId);
        return columns.stream()
            .filter(col -> col.getName() == BoardColumnNameEnum.INITIALIZED)
            .findFirst()
            .orElseThrow(() -> new SQLException("Coluna inicial não encontrada"));
    }
    
    public BoardColumnEntity findPendingColumn() throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        Long firstBoardId = getFirstBoardId();
        List<BoardColumnEntity> columns = dao.findByBoardId(firstBoardId);
        return columns.stream()
            .filter(col -> col.getName() == BoardColumnNameEnum.PENDING)
            .findFirst()
            .orElseThrow(() -> new SQLException("Coluna pendente não encontrada"));
    }
    
    private Long getFirstBoardId() throws SQLException {
        String sql = "SELECT MIN(id) FROM BOARDS";
        try (var statement = connection.prepareStatement(sql)) {
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                return null;
            }
        }
    }

    public List<BoardColumnEntity> findByBoardId(final Long boardId) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        try {
            return dao.findByBoardId(boardId);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        try {
            return dao.findById(id);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
