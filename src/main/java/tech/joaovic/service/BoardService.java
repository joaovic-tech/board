package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.BoardDAO;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnTypeEnum;
import tech.joaovic.persistence.entity.BoardEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardService {
    private final Connection connection;

    public void create(final BoardEntity entity, List<String> pendingColumnNames) throws SQLException {
        BoardDAO boardDAO = new BoardDAO(connection);
        try {
            boardDAO.insert(entity);
            createBoardColumns(entity, pendingColumnNames);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public void create(final BoardEntity entity) throws SQLException {
        // Método compatível com implementação atual - cria board com estrutura padrão
        List<String> defaultPendingColumns = List.of("Em Andamento");
        create(entity, defaultPendingColumns);
    }
    
    /**
     * Cria as colunas obrigatórias para um board específico
     * Estrutura: INICIAL -> PENDENTEs... -> FINAL -> CANCELAMENTO
     */
    private void createBoardColumns(BoardEntity board, List<String> pendingColumnNames) throws SQLException {
        BoardColumnService columnService = new BoardColumnService(connection);
        int currentLevel = 1;
        
        // 1. Criar coluna INICIAL (sempre primeira)
        createColumn(columnService, board, BoardColumnTypeEnum.INICIAL, "Inicial", currentLevel++);
        
        // 2. Criar colunas PENDENTE (podem ser várias)
        for (String pendingName : pendingColumnNames) {
            createColumn(columnService, board, BoardColumnTypeEnum.PENDENTE, pendingName, currentLevel++);
        }
        
        // 3. Criar coluna FINAL (sempre penúltima)
        createColumn(columnService, board, BoardColumnTypeEnum.FINAL, "Finalizada", currentLevel++);
        
        // 4. Criar coluna CANCELAMENTO (sempre última)
        createColumn(columnService, board, BoardColumnTypeEnum.CANCELAMENTO, "Cancelada", currentLevel);
    }
    
    private void createColumn(BoardColumnService service, BoardEntity board, BoardColumnTypeEnum type, String name, int nivel) throws SQLException {
        BoardColumnEntity column = new BoardColumnEntity();
        column.setType(type);
        column.setName(name);
        column.setNivel(nivel);
        column.getBoard().setId(board.getId());
        service.createWithoutCommit(column);
    }

    public boolean delete(final Long id) throws SQLException {
        BoardDAO boardDAO = new BoardDAO(connection);
        try {
            if (!boardDAO.exists(id)) {
                return false;
            }
            boardDAO.delete(id);
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    // findById
    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        BoardDAO boardDAO = new BoardDAO(connection);
        try {
            return boardDAO.findById(id);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }


    public List<BoardEntity> findAll() throws SQLException {
        BoardDAO boardDAO = new BoardDAO(connection);
        try {
            return boardDAO.findAll();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
