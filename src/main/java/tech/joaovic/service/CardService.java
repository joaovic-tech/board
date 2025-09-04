package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.CardDAO;
import tech.joaovic.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class CardService {
    private final Connection connection;

    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }
    
    public int countCardsByBoardId(final Long boardId) throws SQLException {
        CardDAO dao = new CardDAO(connection);
        try {
            return dao.countCardsByBoardId(boardId);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
