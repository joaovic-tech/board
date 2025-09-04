package tech.joaovic.ui;

import tech.joaovic.persistence.entity.BoardEntity;
import tech.joaovic.service.BoardService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import static tech.joaovic.persistence.config.ConnectionConfig.getConnection;

public class Menu {
    private static final Scanner scanner = new Scanner(System.in);

    public void execute() throws SQLException {
        System.out.println("Bem vindo ao gerenciador de boards, escolha a opção desejada");
        var option = -1;
        while (true){
            System.out.println("*".repeat(40));
            System.out.println("1 - Criar um novo board");
            System.out.println("2 - Selecionar um board existente");
            System.out.println("3 - Excluir um board");
            System.out.println("0 - Sair");
            option = scanner.nextInt();
            switch (option){
                case 1 -> createBoard();
                case 2 -> boardImpl();
                case 3 -> deleteBoard();
                case 0 -> System.exit(0);
                default -> System.out.println("Opção inválida, informe uma opção do menu");
            }
        }
    }

    private void createBoard() throws SQLException {
        scanner.nextLine();
        
        // Solicitar nome do board
        System.out.print("Informe o nome do board: ");
        String name = scanner.nextLine();

        if (name.trim().isEmpty()) {
            System.out.println("Nome do board não pode estar vazio!");
            return;
        }
        
        // Perguntar se deseja customizar as colunas
        System.out.print("\nDeseja customizar as colunas do board? (s/N): ");
        String customizeChoice = scanner.nextLine().toLowerCase();
        
        List<String> pendingColumnNames;
        if ("s".equals(customizeChoice) || "sim".equals(customizeChoice)) {
            pendingColumnNames = createCustomColumns();
        } else {
            // Usar colunas padrão
            pendingColumnNames = List.of("Em Andamento", "Concluída");
        }
        
        // Mostrar resumo do board que será criado
        displayBoardSummary(name.trim(), pendingColumnNames);
        
        // Confirmar criação
        System.out.print("\nConfirmar criação do board? (s/N): ");
        String confirmation = scanner.nextLine().toLowerCase();
        
        if (!"s".equals(confirmation) && !"sim".equals(confirmation)) {
            System.err.println("❌ Criação do board cancelada.");
            return;
        }

        // Criar o board
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setName(name.trim());
        
        try(Connection connection = getConnection()){
            BoardService service = new BoardService(connection);
            service.create(boardEntity, pendingColumnNames);
            System.out.println("✅ Board criado com sucesso!");
        }
    }
    
    private List<String> createCustomColumns() {
        List<String> columnNames = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        
        // Solicitar número de colunas pendentes
        int numPendingColumns;
        do {
            System.out.print("\nQuantas colunas pendentes deseja? (mínimo 1, máximo 8): ");
            numPendingColumns = scanner.nextInt();
            scanner.nextLine(); // limpar buffer
            
            if (numPendingColumns < 1 || numPendingColumns > 8) {
                System.err.println("❌ Número inválido! Deve estar entre 1 e 8.");
            }
        } while (numPendingColumns < 1 || numPendingColumns > 8);
        
        // Solicitar nome de cada coluna pendente
        System.out.println("\n📝 Nomeando as colunas pendentes:");
        for (int i = 1; i <= numPendingColumns; i++) {
            String columnName;
            do {
                System.out.printf("   %d. Nome da %dª coluna pendente: ", i, i);
                columnName = scanner.nextLine().trim();
                
                if (columnName.isEmpty()) {
                    System.err.println("   ❌ Nome não pode estar vazio!");
                    continue;
                }
                
                if (usedNames.contains(columnName.toLowerCase())) {
                    System.err.println("   ❌ Nome já utilizado! Escolha um nome diferente.");
                    continue;
                }
                
                // Verificar se não conflita com nomes reservados
                String lowerName = columnName.toLowerCase();
                if (lowerName.equals("inicial") || lowerName.equals("finalizada") || 
                    lowerName.equals("cancelada") || lowerName.equals("cancelamento")) {
                    System.err.println("   ❌ Nome reservado! Use um nome diferente.");
                    continue;
                }
                
                break;
            } while (true);
            
            columnNames.add(columnName);
            usedNames.add(columnName.toLowerCase());
        }
        
        return columnNames;
    }
    
    private void displayBoardSummary(String boardName, List<String> pendingColumnNames) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESUMO DO BOARD QUE SERÁ CRIADO");
        System.out.println("=".repeat(50));
        System.out.printf("📌 Nome: %s\n", boardName);
        System.out.printf("📂 Total de colunas: %d\n", pendingColumnNames.size() + 3); // +3 para inicial, final, cancelamento
        System.out.println("\n🔄 Estrutura das colunas:");
        System.out.println("   1. 📥 Inicial");
        
        for (int i = 0; i < pendingColumnNames.size(); i++) {
            System.out.printf("   %d. ⚡ %s\n", i + 2, pendingColumnNames.get(i));
        }
        
        System.out.printf("   %d. ✅ Finalizada\n", pendingColumnNames.size() + 2);
        System.out.printf("   %d. ❌ Cancelada\n", pendingColumnNames.size() + 3);
        System.out.println("=".repeat(50));
    }

    private List<BoardEntity> selectBoard(BoardService service) throws SQLException {
        try {
            List<BoardEntity> boards = service.findAll();
            System.out.println("Selecione um board:");
            boards.forEach(board -> {
                System.out.printf("%d - %s\n", board.getId(), board.getName());
            });
            return boards;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void boardImpl() throws SQLException {
        try(Connection connection = getConnection()){
            BoardService service = new BoardService(connection);
            List<BoardEntity> boards = selectBoard(service);

            if (boards.isEmpty()) {
                System.err.println("Nenhum board encontrado. Crie um board primeiro.");
                return;
            }
            
            System.out.print("Digite o ID do board: ");
            Long boardId = scanner.nextLong();
            Optional<BoardEntity> optional = service.findById(boardId);
            optional.ifPresentOrElse(
                    board -> {
                        new BoardMenu(board).execute();
                    },
                    () -> System.err.printf("Não foi encontrado um board com id %s\n", boardId));

        }
    }

    private void deleteBoard() throws SQLException {
        try(Connection connection = getConnection()){
            BoardService service = new BoardService(connection);
            List<BoardEntity> boards = selectBoard(service);

            if (boards.isEmpty()) {
                System.err.println("Nenhum board encontrado. Crie um board primeiro.");
                return;
            }

            System.out.print("Informe o id do Board que deseja excluir: ");
            Long id = scanner.nextLong();

            if (!service.delete(id)) {
                System.err.println("Board não encontrado!");
                return;
            }
            System.out.println("Board excluído com sucesso!");
        }
    }
}
