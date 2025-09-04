package tech.joaovic.ui;

import tech.joaovic.persistence.entity.BoardColumnEntity;
import tech.joaovic.persistence.entity.BoardEntity;
import tech.joaovic.persistence.entity.CardEntity;
import tech.joaovic.service.BoardColumnService;
import tech.joaovic.service.CardService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
        System.out.println("0 - Voltar");
        System.out.print("Opção: ");
    }
    
    private void executeOption(int option) throws SQLException {
        switch (option) {
            case 1 -> createCard();
            case 2 -> listCards();
            case 3 -> moveCard();
            case 4 -> toggleCardBlock();
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
    
    private void moveCard() {
        System.out.println("Funcionalidade em desenvolvimento: Mover card");
    }
    
    private void toggleCardBlock() {
        System.out.println("Funcionalidade em desenvolvimento: Bloquear/Desbloquear card");
    }
    
    private void handleError(SQLException e) {
        System.err.println("Erro: " + e.getMessage());
    }
}
