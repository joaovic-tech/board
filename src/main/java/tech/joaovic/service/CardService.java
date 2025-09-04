package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.CardDAO;
import tech.joaovic.persistence.dao.CardMovementDAO;
import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnTypeEnum;
import tech.joaovic.persistence.entity.CardEntity;
import tech.joaovic.persistence.entity.CardMovementEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    
    public List<CardEntity> findCardsByBoardId(final Long boardId) throws SQLException {
        CardDAO dao = new CardDAO(connection);
        try {
            return dao.findCardsByBoardId(boardId);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public Optional<CardEntity> findById(final Long cardId) throws SQLException {
        CardDAO dao = new CardDAO(connection);
        try {
            return dao.findById(cardId);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public void moveCard(final Long cardId, final Long newColumnId) throws SQLException {
        CardDAO cardDAO = new CardDAO(connection);
        CardMovementDAO movementDAO = new CardMovementDAO(connection);
        BoardColumnService columnService = new BoardColumnService(connection);
        
        try {
            // Verificar se o card existe
            Optional<CardEntity> cardOpt = cardDAO.findById(cardId);
            if (cardOpt.isEmpty()) {
                throw new SQLException("Card não encontrado com ID: " + cardId);
            }
            
            CardEntity card = cardOpt.get();
            BoardColumnEntity currentColumn = card.getBoardColumn();
            
            // Verificar se o card não está bloqueado
            if ("F".equals(card.getStatus())) {
                throw new SQLException("Card está bloqueado e não pode ser movido");
            }
            
            // Buscar a nova coluna
            Optional<BoardColumnEntity> newColumnOpt = columnService.findById(newColumnId);
            if (newColumnOpt.isEmpty()) {
                throw new SQLException("Coluna de destino não encontrada com ID: " + newColumnId);
            }
            
            // Registrar movimento no histórico
            CardMovementEntity movement = new CardMovementEntity();
            movement.getCard().setId(cardId);
            movement.setFromColumn(currentColumn);
            movement.setToColumn(newColumnOpt.get());
            movementDAO.insert(movement);
            
            // Atualizar a coluna do card
            cardDAO.updateColumn(cardId, newColumnId);
            connection.commit();
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public void cancelCard(final Long cardId) throws SQLException {
        CardDAO cardDAO = new CardDAO(connection);
        try {
            // Verificar se o card existe
            Optional<CardEntity> cardOpt = cardDAO.findById(cardId);
            if (cardOpt.isEmpty()) {
                throw new SQLException("Card não encontrado com ID: " + cardId);
            }
            
            CardEntity card = cardOpt.get();
            BoardColumnEntity currentColumn = card.getBoardColumn();
            
            // Verificar se o card não está bloqueado
            if ("F".equals(card.getStatus())) {
                throw new SQLException("Card está bloqueado e não pode ser cancelado");
            }
            
            // Verificar se o card não está em coluna FINAL (não pode ser cancelado)
            if (currentColumn.getType() == BoardColumnTypeEnum.FINAL) {
                throw new SQLException("Cards finalizados não podem ser cancelados");
            }
            
            // Verificar se o card já está cancelado
            if (currentColumn.getType() == BoardColumnTypeEnum.CANCELAMENTO) {
                throw new SQLException("Card já está cancelado");
            }
            
            // Buscar a coluna de cancelamento do board
            Long boardId = currentColumn.getBoard().getId();
            BoardColumnService columnService = new BoardColumnService(connection);
            BoardColumnEntity cancelColumn = columnService.findCancelColumnByBoardId(boardId);
            
            // Mover o card para a coluna de cancelamento
            cardDAO.updateColumn(cardId, cancelColumn.getId());
            connection.commit();
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
