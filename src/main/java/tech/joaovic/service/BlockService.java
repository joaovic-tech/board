package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.BlockDAO;
import tech.joaovic.persistence.dao.CardDAO;
import tech.joaovic.persistence.entity.BlockEntity;
import tech.joaovic.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BlockService {
    private final Connection connection;
    
    public void blockCard(final Long cardId, final String blockReason) throws SQLException {
        CardDAO cardDAO = new CardDAO(connection);
        BlockDAO blockDAO = new BlockDAO(connection);
        
        try {
            // Verificar se o card existe
            Optional<CardEntity> cardOpt = cardDAO.findById(cardId);
            if (cardOpt.isEmpty()) {
                throw new SQLException("Card não encontrado com ID: " + cardId);
            }
            
            CardEntity card = cardOpt.get();
            
            // Verificar se o card já está bloqueado
            if ("F".equals(card.getStatus())) {
                throw new SQLException("Card já está bloqueado");
            }
            
            // Verificar se há bloqueio ativo
            Optional<BlockEntity> activeBlock = blockDAO.findActiveBlockByCardId(cardId);
            if (activeBlock.isPresent()) {
                throw new SQLException("Card já possui um bloqueio ativo");
            }
            
            // Criar registro de bloqueio
            BlockEntity blockEntity = new BlockEntity();
            blockEntity.setBlockReason(blockReason);
            blockEntity.getCard().setId(cardId);
            
            // Atualizar status do card para bloqueado
            cardDAO.updateStatus(cardId, "F");
            
            // Inserir registro de bloqueio
            blockDAO.insert(blockEntity);
            
            connection.commit();
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public void unblockCard(final Long cardId, final String unblockReason) throws SQLException {
        CardDAO cardDAO = new CardDAO(connection);
        BlockDAO blockDAO = new BlockDAO(connection);
        
        try {
            // Verificar se o card existe
            Optional<CardEntity> cardOpt = cardDAO.findById(cardId);
            if (cardOpt.isEmpty()) {
                throw new SQLException("Card não encontrado com ID: " + cardId);
            }
            
            CardEntity card = cardOpt.get();
            
            // Verificar se o card está bloqueado
            if (!"F".equals(card.getStatus())) {
                throw new SQLException("Card não está bloqueado");
            }
            
            // Buscar bloqueio ativo
            Optional<BlockEntity> activeBlockOpt = blockDAO.findActiveBlockByCardId(cardId);
            if (activeBlockOpt.isEmpty()) {
                throw new SQLException("Não foi encontrado bloqueio ativo para este card");
            }
            
            BlockEntity activeBlock = activeBlockOpt.get();
            
            // Atualizar status do card para desbloqueado
            cardDAO.updateStatus(cardId, "T");
            
            // Finalizar o bloqueio com motivo de desbloqueio
            blockDAO.updateUnblock(activeBlock.getId(), unblockReason);
            
            connection.commit();
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    
    public Optional<BlockEntity> getActiveBlock(final Long cardId) throws SQLException {
        BlockDAO blockDAO = new BlockDAO(connection);
        try {
            return blockDAO.findActiveBlockByCardId(cardId);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
