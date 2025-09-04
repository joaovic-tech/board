package tech.joaovic.service;

import lombok.AllArgsConstructor;
import tech.joaovic.persistence.dao.BlockDAO;
import tech.joaovic.persistence.dao.CardDAO;
import tech.joaovic.persistence.dao.CardMovementDAO;
import tech.joaovic.persistence.entity.BlockEntity;
import tech.joaovic.persistence.entity.CardEntity;
import tech.joaovic.persistence.entity.CardMovementEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
public class ReportService {
    private final Connection connection;
    
    public void generateBlockReport(Long boardId) throws SQLException {
        BlockDAO blockDAO = new BlockDAO(connection);
        List<BlockEntity> blocks = blockDAO.findByBoardId(boardId);
        
        System.out.println("\n📊 RELATÓRIO DE BLOQUEIOS");
        System.out.println("=" .repeat(80));
        
        if (blocks.isEmpty()) {
            System.out.println("📋 Nenhum bloqueio encontrado neste board.");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        for (BlockEntity block : blocks) {
            System.out.printf("\n🔒 BLOQUEIO #%d\n", block.getId());
            System.out.printf("   📝 Card: %s (ID: %d)\n", 
                block.getCard().getTitle(), block.getCard().getId());
            System.out.printf("   👤 Usuário: %s\n", block.getUser());
            System.out.printf("   📋 Motivo: %s\n", block.getBlockReason());
            System.out.printf("   📅 Criado em: %s\n", 
                block.getBlockedAt().format(formatter));
            
            if (block.getUnblockAt() != null) {
                System.out.printf("   🔓 Desbloqueado em: %s\n", 
                    block.getUnblockAt().format(formatter));
                System.out.printf("   📋 Motivo do desbloqueio: %s\n", 
                    block.getUnblockedReason());
                
                // Calcular duração do bloqueio
                Duration blockDuration = Duration.between(
                    block.getBlockedAt(), block.getUnblockAt());
                System.out.printf("   ⏱️  Duração do bloqueio: %s\n", 
                    formatDuration(blockDuration));
            } else {
                System.out.println("   🔒 Status: BLOQUEADO (ativo)");
                
                // Calcular tempo desde o bloqueio
                Duration sinceBlock = Duration.between(
                    block.getBlockedAt(), OffsetDateTime.now());
                System.out.printf("   ⏱️  Bloqueado há: %s\n", 
                    formatDuration(sinceBlock));
            }
            
            System.out.println("-".repeat(50));
        }
        
        System.out.printf("\n📈 RESUMO: %d bloqueio(s) encontrado(s)\n", blocks.size());
    }
    
    public void generateMovementReport(Long boardId) throws SQLException {
        CardMovementDAO movementDAO = new CardMovementDAO(connection);
        List<CardMovementEntity> movements = movementDAO.findByBoardId(boardId);
        
        System.out.println("\n📊 RELATÓRIO DE MOVIMENTAÇÕES");
        System.out.println("=" .repeat(80));
        
        if (movements.isEmpty()) {
            System.out.println("📋 Nenhuma movimentação encontrada neste board.");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        for (CardMovementEntity movement : movements) {
            System.out.printf("\n🔄 MOVIMENTAÇÃO #%d\n", movement.getId());
            System.out.printf("   📝 Card: %s (ID: %d)\n", 
                movement.getCard().getTitle(), movement.getCard().getId());
            
            if (movement.getFromColumn() != null) {
                System.out.printf("   📤 De: %s (%s)\n", 
                    movement.getFromColumn().getName(), 
                    movement.getFromColumn().getType().name());
            } else {
                System.out.printf("   📤 De: (CRIAÇÃO INICIAL)\n");
            }
            
            System.out.printf("   📥 Para: %s (%s)\n", 
                movement.getToColumn().getName(), 
                movement.getToColumn().getType().name());
            System.out.printf("   📅 Data/Hora: %s\n", 
                movement.getMovedAt().format(formatter));
            
            System.out.println("-".repeat(50));
        }
        
        System.out.printf("\n📈 RESUMO: %d movimentação(ões) encontrada(s)\n", movements.size());
    }
    
    public void generateLifetimeReport(Long boardId) throws SQLException {
        CardDAO cardDAO = new CardDAO(connection);
        List<CardEntity> cards = cardDAO.findCardsByBoardId(boardId);
        
        System.out.println("\n📊 RELATÓRIO DE TEMPO DE VIDA DOS CARDS");
        System.out.println("=" .repeat(80));
        
        if (cards.isEmpty()) {
            System.out.println("📋 Nenhum card encontrado neste board.");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        OffsetDateTime now = OffsetDateTime.now();
        
        int completedCards = 0;
        int activeCards = 0;
        
        for (CardEntity card : cards) {
            System.out.printf("\n📝 CARD: %s (ID: %d)\n", card.getTitle(), card.getId());
            System.out.printf("   📂 Coluna atual: %s (%s)\n", 
                card.getBoardColumn().getName(), 
                card.getBoardColumn().getType().name());
            System.out.printf("   📅 Criado em: %s\n", 
                card.getCreatedAt().format(formatter));
            
            // Verificar se o card está concluído (FINAL ou CANCELAMENTO)
            boolean isCompleted = card.getBoardColumn().getType().name().equals("FINAL") || 
                                card.getBoardColumn().getType().name().equals("CANCELAMENTO");
            
            if (isCompleted) {
                completedCards++;
                // Para cards concluídos, usar moved_at como data de conclusão
                OffsetDateTime completionDate = card.getMovedAt() != null ? 
                    card.getMovedAt() : card.getCreatedAt();
                
                Duration lifetime = Duration.between(card.getCreatedAt(), completionDate);
                System.out.printf("   ✅ Status: CONCLUÍDO\n");
                System.out.printf("   📅 Concluído em: %s\n", 
                    completionDate.format(formatter));
                System.out.printf("   ⏱️  Tempo de vida: %s\n", 
                    formatDuration(lifetime));
            } else {
                activeCards++;
                Duration lifetime = Duration.between(card.getCreatedAt(), now);
                System.out.printf("   🔄 Status: EM ANDAMENTO\n");
                System.out.printf("   ⏱️  Tempo em andamento: %s\n", 
                    formatDuration(lifetime));
            }
            
            System.out.println("-".repeat(50));
        }
        
        System.out.printf("\n📈 RESUMO:\n");
        System.out.printf("   📝 Total de cards: %d\n", cards.size());
        System.out.printf("   ✅ Cards concluídos: %d\n", completedCards);
        System.out.printf("   🔄 Cards em andamento: %d\n", activeCards);
    }
    
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }
        
        return sb.toString().trim();
    }
}
