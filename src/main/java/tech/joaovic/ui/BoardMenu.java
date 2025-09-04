package tech.joaovic.ui;

import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardColumnTypeEnum;
import tech.joaovic.persistence.entity.BoardEntity;
import tech.joaovic.persistence.entity.CardEntity;
import tech.joaovic.service.BlockService;
import tech.joaovic.service.BoardColumnService;
import tech.joaovic.service.CardService;
import tech.joaovic.service.ReportService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import static tech.joaovic.persistence.config.ConnectionConfig.getConnection;

public class BoardMenu {
    private final BoardEntity board;
    private final Scanner scanner;
    
    public BoardMenu(BoardEntity board) {
        this.board = board;
        this.scanner = new Scanner(System.in);
    }

    public void execute() {
        try {
            displayWelcomeMessage();
            processMenuOptions();
        } catch (SQLException e) {
            handleError(e);
        }
    }
    
    private void displayWelcomeMessage() {
        System.out.printf("=== Board: %s ===\n", board.getName());
    }
    
    private void processMenuOptions() throws SQLException {
        int option = -1;
        while (option != 0) {
            displayMenu();
            option = scanner.nextInt();
            executeOption(option);
        }
    }
    
    private void displayMenu() {
        System.out.println("*".repeat(40));
        System.out.println("1 - Criar card");
        System.out.println("2 - Listar cards");
        System.out.println("3 - Mover card");
        System.out.println("4 - Bloquear/Desbloquear card");
        System.out.println("5 - Cancelar card");
        System.out.println("6 - Relatório de bloqueios");
        System.out.println("7 - Relatório de movimentações");
        System.out.println("8 - Relatório de tempo de vida");
        System.out.println("0 - Voltar");
        System.out.print("Opção: ");
    }
    
    private void executeOption(int option) throws SQLException {
        switch (option) {
            case 1 -> createCard();
            case 2 -> listCards();
            case 3 -> moveCard();
            case 4 -> toggleCardBlock();
            case 5 -> cancelCard();
            case 6 -> showBlockReport();
            case 7 -> showMovementReport();
            case 8 -> showLifetimeReport();
            case 0 -> System.out.println("Voltando...");
            default -> System.out.println("Opção inválida!");
        }
    }

    private void createCard() throws SQLException {
        try (var connection = getConnection()) {
            var columnService = new BoardColumnService(connection);
            var cardService = new CardService(connection);
            
            int existingCardsCount = cardService.countCardsByBoardId(board.getId());
            BoardColumnEntity targetColumn = determineTargetColumn(columnService, existingCardsCount);
            
            CardEntity card = buildCard(targetColumn);
            cardService.create(card);
            
            System.out.printf("Card '%s' criado na coluna %s!\n", 
                card.getTitle(), targetColumn.getName());
        }
    }
    
    private BoardColumnEntity determineTargetColumn(BoardColumnService columnService, int existingCardsCount) throws SQLException {
        // Todo card novo sempre entra na coluna inicial do board
        return columnService.findInitialColumnByBoardId(board.getId());
    }
    
    private CardEntity buildCard(BoardColumnEntity column) {
        scanner.nextLine();
        
        System.out.print("Título: ");
        String title = scanner.nextLine();
        
        System.out.print("Descrição: ");
        String description = scanner.nextLine();
        
        CardEntity card = new CardEntity();
        card.setTitle(title);
        card.setDescription(description);
        card.setStatus("T");
        card.setBoardColumn(column);
        column.getBoard().setId(board.getId());
        
        return card;
    }
    
    private void listCards() throws SQLException {
        try (var connection = getConnection()) {
            var cardService = new CardService(connection);
            var columnService = new BoardColumnService(connection);
            
            List<CardEntity> cards = cardService.findCardsByBoardId(board.getId());
            List<BoardColumnEntity> columns = columnService.findByBoardId(board.getId());
            
            if (cards.isEmpty()) {
                System.out.println("\n📋 Nenhum card encontrado neste board.");
                return;
            }
            
            displayBoardStructure(cards, columns);
        }
    }
    
    private void displayBoardStructure(List<CardEntity> cards, List<BoardColumnEntity> columns) {
        System.out.printf("\n📊 Board: %s\n", board.getName());
        System.out.println("=" .repeat(60));
        
        // Agrupar cards por coluna
        Map<Long, List<CardEntity>> cardsByColumn = cards.stream()
            .collect(Collectors.groupingBy(card -> card.getBoardColumn().getId()));
        
        // Exibir cada coluna com seus cards
        columns.forEach(column -> displayColumn(column, cardsByColumn.get(column.getId())));
    }
    
    private void displayColumn(BoardColumnEntity column, List<CardEntity> columnCards) {
        String columnHeader = String.format("📂 %s (%s)", column.getName(), column.getType().name());
        System.out.println("\n" + columnHeader);
        System.out.println("-".repeat(columnHeader.length()));
        
        if (columnCards == null || columnCards.isEmpty()) {
            System.out.println("   (vazia)");
            return;
        }
        
        columnCards.forEach(this::displayCard);
    }
    
    private void displayCard(CardEntity card) {
        String statusIcon = "T".equals(card.getStatus()) ? "✅" : "🚫";
        String createdAt = card.getCreatedAt() != null 
            ? card.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            : "N/A";
            
        System.out.printf("   %s [ID: %d] %s\n", statusIcon, card.getId(), card.getTitle());
        System.out.printf("      📝 %s\n", card.getDescription());
        System.out.printf("      🕐 Criado em: %s\n", createdAt);
        
        if (card.getMovedAt() != null) {
            String movedAt = card.getMovedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            System.out.printf("      🔄 Última movimentação: %s\n", movedAt);
        }
        
        System.out.println();
    }
    
    private void moveCard() throws SQLException {
        try (var connection = getConnection()) {
            var cardService = new CardService(connection);
            var columnService = new BoardColumnService(connection);
            
            // Listar cards disponíveis para movimentação
            List<CardEntity> cards = cardService.findCardsByBoardId(board.getId());
            if (cards.isEmpty()) {
                System.out.println("\n📋 Nenhum card encontrado neste board.");
                return;
            }
            
            // Filtrar apenas cards que podem ser movidos (não estão em FINAL ou CANCELAMENTO)
            List<CardEntity> moveableCards = cards.stream()
                .filter(card -> card.getBoardColumn().getType() != BoardColumnTypeEnum.FINAL &&
                               card.getBoardColumn().getType() != BoardColumnTypeEnum.CANCELAMENTO)
                .toList();
            
            if (moveableCards.isEmpty()) {
                System.out.println("\n❌ Nenhum card pode ser movido. Todos os cards estão em colunas finais.");
                return;
            }
            
            // Mostrar cards disponíveis
            System.out.println("\n🔄 Cards disponíveis para movimentação:");
            for (int i = 0; i < moveableCards.size(); i++) {
                CardEntity card = moveableCards.get(i);
                String statusIcon = "T".equals(card.getStatus()) ? "✅" : "🚫";
                System.out.printf("%d - %s [ID: %d] %s (Coluna: %s)\n", 
                    i + 1, statusIcon, card.getId(), card.getTitle(), card.getBoardColumn().getName());
            }
            
            // Selecionar card
            System.out.print("\nEscolha o card (número): ");
            int cardChoice = scanner.nextInt() - 1;
            
            if (cardChoice < 0 || cardChoice >= moveableCards.size()) {
                System.out.println("❌ Opção inválida!");
                return;
            }
            
            CardEntity selectedCard = moveableCards.get(cardChoice);
            
            // Verificar se o card está bloqueado
            if ("F".equals(selectedCard.getStatus())) {
                System.out.println("❌ Este card está bloqueado e não pode ser movido!");
                return;
            }
            
            // Obter opções de movimento válidas
            List<BoardColumnEntity> validDestinations = columnService.getValidMoveOptions(
                board.getId(), selectedCard.getBoardColumn());
            
            if (validDestinations.isEmpty()) {
                System.out.println("❌ Não há movimentos válidos para este card.");
                return;
            }
            
            // Mostrar opções de destino
            System.out.println("\n🎯 Destinos disponíveis:");
            for (int i = 0; i < validDestinations.size(); i++) {
                BoardColumnEntity dest = validDestinations.get(i);
                String typeDescription = switch (dest.getType()) {
                    case INICIAL -> "Inicial";
                    case PENDENTE -> "Em progresso";
                    case FINAL -> "Finalizada";
                    case CANCELAMENTO -> "Cancelar card";
                };
                System.out.printf("%d - %s (%s)\n", i + 1, dest.getName(), typeDescription);
            }
            
            // Selecionar destino
            System.out.print("\nEscolha o destino (número): ");
            int destChoice = scanner.nextInt() - 1;
            
            if (destChoice < 0 || destChoice >= validDestinations.size()) {
                System.out.println("❌ Opção inválida!");
                return;
            }
            
            BoardColumnEntity targetColumn = validDestinations.get(destChoice);
            
            // Executar movimento
            cardService.moveCard(selectedCard.getId(), targetColumn.getId());
            
            System.out.printf("✅ Card '%s' movido para '%s' com sucesso!\n", 
                selectedCard.getTitle(), targetColumn.getName());
        }
    }
    
    private void toggleCardBlock() throws SQLException {
        try (var connection = getConnection()) {
            var cardService = new CardService(connection);
            var blockService = new BlockService(connection);
            
            // Listar todos os cards do board
            List<CardEntity> cards = cardService.findCardsByBoardId(board.getId());
            if (cards.isEmpty()) {
                System.out.println("\n📋 Nenhum card encontrado neste board.");
                return;
            }
            
            // Mostrar cards disponíveis com status
            System.out.println("\n🔐 Gerenciar bloqueio de cards:");
            for (int i = 0; i < cards.size(); i++) {
                CardEntity card = cards.get(i);
                String statusText = "T".equals(card.getStatus()) ? "🔓 Desbloqueado" : "🔒 Bloqueado";
                System.out.printf("%d - [ID: %d] %s - %s (Coluna: %s)\n", 
                    i + 1, card.getId(), card.getTitle(), statusText, card.getBoardColumn().getName());
            }
            
            // Selecionar card
            System.out.print("\nEscolha o card (número): ");
            int cardChoice = scanner.nextInt() - 1;
            
            if (cardChoice < 0 || cardChoice >= cards.size()) {
                System.out.println("❌ Opção inválida!");
                return;
            }
            
            CardEntity selectedCard = cards.get(cardChoice);

            if ("T".equals(selectedCard.getStatus())) {
                blockCard(blockService, selectedCard);
            } else {
                unblockCard(blockService, selectedCard);
            }
        }
    }
    
    private void blockCard(BlockService blockService, CardEntity card) throws SQLException {
        scanner.nextLine(); // limpar buffer
        
        System.out.printf("\n🔒 Bloqueando card: %s\n", card.getTitle());
        System.out.print("Informe o motivo do bloqueio: ");
        String blockReason = scanner.nextLine();
        
        if (blockReason.trim().isEmpty()) {
            System.out.println("❌ Motivo do bloqueio é obrigatório!");
            return;
        }
        
        try {
            blockService.blockCard(card.getId(), blockReason.trim());
            System.out.printf("✅ Card '%s' foi bloqueado com sucesso!\n", card.getTitle());
            System.out.printf("   📝 Motivo: %s\n", blockReason.trim());
        } catch (SQLException e) {
            System.err.printf("❌ Erro ao bloquear card: %s\n", e.getMessage());
        }
    }
    
    private void unblockCard(BlockService blockService, CardEntity card) throws SQLException {
        scanner.nextLine(); // limpar buffer
        
        System.out.printf("\n🔓 Desbloqueando card: %s\n", card.getTitle());
        System.out.print("Informe o motivo do desbloqueio: ");
        String unblockReason = scanner.nextLine();
        
        if (unblockReason.trim().isEmpty()) {
            System.out.println("❌ Motivo do desbloqueio é obrigatório!");
            return;
        }
        
        try {
            blockService.unblockCard(card.getId(), unblockReason.trim());
            System.out.printf("✅ Card '%s' foi desbloqueado com sucesso!\n", card.getTitle());
            System.out.printf("   📝 Motivo: %s\n", unblockReason.trim());
        } catch (SQLException e) {
            System.err.printf("❌ Erro ao desbloquear card: %s\n", e.getMessage());
        }
    }
    
    private void cancelCard() throws SQLException {
        try (var connection = getConnection()) {
            var cardService = new CardService(connection);
            
            // Listar cards disponíveis para cancelamento
            List<CardEntity> cards = cardService.findCardsByBoardId(board.getId());
            if (cards.isEmpty()) {
                System.out.println("\n📋 Nenhum card encontrado neste board.");
                return;
            }
            
            // Filtrar cards que podem ser cancelados (não estão em FINAL ou CANCELAMENTO)
            List<CardEntity> cancelableCards = cards.stream()
                .filter(card -> card.getBoardColumn().getType() != BoardColumnTypeEnum.FINAL &&
                               card.getBoardColumn().getType() != BoardColumnTypeEnum.CANCELAMENTO)
                .toList();
            
            if (cancelableCards.isEmpty()) {
                System.out.println("\n❌ Nenhum card pode ser cancelado. Cards finalizados ou já cancelados não podem ser cancelados.");
                return;
            }
            
            // Mostrar cards disponíveis para cancelamento
            System.out.println("\n❌ Cards disponíveis para cancelamento:");
            for (int i = 0; i < cancelableCards.size(); i++) {
                CardEntity card = cancelableCards.get(i);
                String statusIcon = "T".equals(card.getStatus()) ? "✅" : "🚫";
                System.out.printf("%d - %s [ID: %d] %s (Coluna: %s)\n", 
                    i + 1, statusIcon, card.getId(), card.getTitle(), card.getBoardColumn().getName());
            }
            
            // Selecionar card
            System.out.print("\nEscolha o card para cancelar (número): ");
            int cardChoice = scanner.nextInt() - 1;
            
            if (cardChoice < 0 || cardChoice >= cancelableCards.size()) {
                System.out.println("❌ Opção inválida!");
                return;
            }
            
            CardEntity selectedCard = cancelableCards.get(cardChoice);
            
            // Verificar se o card está bloqueado
            if ("F".equals(selectedCard.getStatus())) {
                System.out.println("❌ Este card está bloqueado e não pode ser cancelado!");
                return;
            }
            
            // Confirmar cancelamento
            System.out.printf("\n⚠️  Tem certeza que deseja cancelar o card '%s'? (s/N): ", selectedCard.getTitle());
            scanner.nextLine(); // limpar buffer
            String confirmation = scanner.nextLine().toLowerCase();
            
            if (!"s".equals(confirmation) && !"sim".equals(confirmation)) {
                System.out.println("❌ Cancelamento abortado pelo usuário.");
                return;
            }
            
            // Executar cancelamento
            try {
                cardService.cancelCard(selectedCard.getId());
                System.out.printf("✅ Card '%s' foi cancelado com sucesso!\n", selectedCard.getTitle());
                System.out.println("   📝 O card foi movido para a coluna de cancelamento.");
            } catch (SQLException e) {
                System.err.printf("❌ Erro ao cancelar card: %s\n", e.getMessage());
            }
        }
    }
    
    private void showBlockReport() throws SQLException {
        try (var connection = getConnection()) {
            var reportService = new ReportService(connection);
            reportService.generateBlockReport(board.getId());
            
            System.out.println("\n\n🖱️  Pressione Enter para continuar...");
            scanner.nextLine();
            scanner.nextLine();
        }
    }
    
    private void showMovementReport() throws SQLException {
        try (var connection = getConnection()) {
            var reportService = new ReportService(connection);
            reportService.generateMovementReport(board.getId());
            
            System.out.println("\n\n🖱️  Pressione Enter para continuar...");
            scanner.nextLine();
            scanner.nextLine();
        }
    }
    
    private void showLifetimeReport() throws SQLException {
        try (var connection = getConnection()) {
            var reportService = new ReportService(connection);
            reportService.generateLifetimeReport(board.getId());
            
            System.out.println("\n\n🖱️  Pressione Enter para continuar...");
            scanner.nextLine();
            scanner.nextLine();
        }
    }
    
    private void handleError(SQLException e) {
        System.err.println("Erro: " + e.getMessage());
    }
}
