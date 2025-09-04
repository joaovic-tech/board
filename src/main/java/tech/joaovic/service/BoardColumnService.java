package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.BoardColumnDAO;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnTypeEnum;

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
    
    /**
     * Encontra a coluna inicial de um board específico
     */
    public BoardColumnEntity findInitialColumnByBoardId(final Long boardId) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        List<BoardColumnEntity> columns = dao.findByBoardId(boardId);
        return columns.stream()
            .filter(col -> col.getType() == BoardColumnTypeEnum.INICIAL)
            .findFirst()
            .orElseThrow(() -> new SQLException("Coluna inicial não encontrada para o board " + boardId));
    }
    
    /**
     * Encontra a primeira coluna pendente de um board específico
     */
    public BoardColumnEntity findFirstPendingColumnByBoardId(final Long boardId) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        List<BoardColumnEntity> columns = dao.findByBoardId(boardId);
        return columns.stream()
            .filter(col -> col.getType() == BoardColumnTypeEnum.PENDENTE)
            .findFirst()
            .orElseThrow(() -> new SQLException("Coluna pendente não encontrada para o board " + boardId));
    }
    
    /**
     * Encontra a coluna final de um board específico
     */
    public BoardColumnEntity findFinalColumnByBoardId(final Long boardId) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        List<BoardColumnEntity> columns = dao.findByBoardId(boardId);
        return columns.stream()
            .filter(col -> col.getType() == BoardColumnTypeEnum.FINAL)
            .findFirst()
            .orElseThrow(() -> new SQLException("Coluna final não encontrada para o board " + boardId));
    }
    
    /**
     * Encontra a coluna de cancelamento de um board específico
     */
    public BoardColumnEntity findCancelColumnByBoardId(final Long boardId) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        List<BoardColumnEntity> columns = dao.findByBoardId(boardId);
        return columns.stream()
            .filter(col -> col.getType() == BoardColumnTypeEnum.CANCELAMENTO)
            .findFirst()
            .orElseThrow(() -> new SQLException("Coluna de cancelamento não encontrada para o board " + boardId));
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
    
    /**
     * Encontra a próxima coluna válida na sequência do board
     */
    public Optional<BoardColumnEntity> findNextColumn(final Long boardId, final int currentLevel) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        List<BoardColumnEntity> columns = dao.findByBoardId(boardId);
        
        return columns.stream()
            .filter(col -> col.getNivel() == currentLevel + 1)
            .findFirst();
    }
    
    /**
     * Verifica se é possível mover um card de uma coluna para outra
     */
    public boolean canMoveCard(BoardColumnEntity fromColumn, BoardColumnEntity toColumn) {
        // Não pode mover de FINAL ou CANCELAMENTO
        if (fromColumn.getType() == BoardColumnTypeEnum.FINAL || 
            fromColumn.getType() == BoardColumnTypeEnum.CANCELAMENTO) {
            return false;
        }
        
        // Pode mover para CANCELAMENTO de qualquer coluna (exceto FINAL)
        if (toColumn.getType() == BoardColumnTypeEnum.CANCELAMENTO) {
            return fromColumn.getType() != BoardColumnTypeEnum.FINAL;
        }
        
        // Movimento sequencial: próximo nível
        return toColumn.getNivel() == fromColumn.getNivel() + 1;
    }
    
    /**
     * Obtém todas as opções de movimento válidas para um card
     */
    public List<BoardColumnEntity> getValidMoveOptions(final Long boardId, final BoardColumnEntity currentColumn) throws SQLException {
        BoardColumnDAO dao = new BoardColumnDAO(connection);
        List<BoardColumnEntity> allColumns = dao.findByBoardId(boardId);
        
        return allColumns.stream()
            .filter(col -> canMoveCard(currentColumn, col))
            .toList();
    }
}
