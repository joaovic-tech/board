package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.BoardDAO;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnNameEnum;
import tech.joaovic.persistence.entity.BoardEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardService {
    private final Connection connection;

    public void create(final BoardEntity entity) throws SQLException {
        BoardDAO boardDAO = new BoardDAO(connection);
        try {
            boardDAO.insert(entity);
            ensureGlobalColumnsExist(entity);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    private void ensureGlobalColumnsExist(BoardEntity firstBoard) throws SQLException {
        BoardColumnService columnService = new BoardColumnService(connection);
        
        if (!columnService.globalColumnsExist()) {
            createGlobalColumn(columnService, firstBoard, BoardColumnNameEnum.INITIALIZED, 1);
            createGlobalColumn(columnService, firstBoard, BoardColumnNameEnum.PENDING, 2);
            createGlobalColumn(columnService, firstBoard, BoardColumnNameEnum.COMPLETED, 3);
            createGlobalColumn(columnService, firstBoard, BoardColumnNameEnum.FINALIZED, 4);
            createGlobalColumn(columnService, firstBoard, BoardColumnNameEnum.CANCELLED, 5);
        }
    }
    
    private void createGlobalColumn(BoardColumnService service, BoardEntity board, BoardColumnNameEnum name, int nivel) throws SQLException {
        BoardColumnEntity column = new BoardColumnEntity();
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
