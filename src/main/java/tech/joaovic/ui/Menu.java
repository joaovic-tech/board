package tech.joaovic.ui;

import tech.joaovic.persistence.entity.BoardEntity;
import tech.joaovic.service.BoardService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
        System.out.print("Informe o nome do board: ");
        String name = scanner.nextLine();

        if (name.trim().isEmpty()) {
            System.out.println("Nome do board não pode estar vazio!");
            return;
        }

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setName(name.trim());
        try(Connection connection = getConnection()){
            BoardService service = new BoardService(connection);
            service.create(boardEntity);
            System.out.println("Board criado com sucesso!");
        }
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
