# Documentação do projeto

| Esboço da aplicação                                   | Diagrama de entidade                 |
|-------------------------------------------------------|--------------------------------------|
| ![Esboço da aplicação](./template/desenho.drawio.svg) | ![Diagrama](./template/board_db.png) |

---

### Tipos das colunas e a sua ordem
1. Inicializada (coluna unica para cada board)
2. Pendente
3. Concluída
4. Finalizada
5. Cancelada

---

### Ações de alterar card de coluna
* Vamos trabalhar com conceitos de níveis para melhor entendimento, no nível 1 temos a coluna inicial, nível 2 a coluna pedente e assim por diante seguindo a Ordem das colunas.

Exemplo de etapas:

1. `card1` criado (`card1` inicial na coluna Inicializada);
2. Aumentar o `card1` resulta em ≥ (`card1` na coluna pendentes);
3. Aumentar o `card1` resulta em ≥ (`card1` na coluna concluídos);
4. Aumentar o `card1` resulta em ≥ (`card1` na coluna finalizados);
5. Aumentar o `card1` resulta em ≥ (`card1` na coluna cancelados);

> Atenção: mesma coisa serve para diminuir o nivel!

---

### Relacionamentos
1. BOARDS (1:n) BOARDS_COLUMN - Um board possui muitas colunas
2. BOARDS_COLUMN (1:n) CARDS - Uma coluna possui muitos cards
3. CARDS (1:n) BLOCKS - Um card pode ter muitos bloqueios/desbloqueios

---

### Estrutura das Tabelas

#### 1. BOARDS
- **id**: `BIGINT AUTO_INCREMENT PRIMARY KEY`
- **name**: `VARCHAR(255) NOT NULL` - Nome do board

#### 2. BOARDS_COLUMN
- **id**: `BIGINT AUTO_INCREMENT PRIMARY KEY`
- **name**: `VARCHAR(255) NOT NULL` - Nome da coluna
- **nivel**: `INT NOT NULL` - Ordem/nível da coluna no board
- **board_id**: `BIGINT NOT NULL` - Foreign Key para BOARDS

#### 3. CARDS
- **id**: `BIGINT AUTO_INCREMENT PRIMARY KEY`
- **title**: `VARCHAR(255) NOT NULL` - Título do card
- **description**: `VARCHAR(255) NOT NULL` - Descrição do card
- **created_at**: `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` - Data de criação
- **moved_at**: `TIMESTAMP NULL` - Data da última movimentação
- **status**: `CHAR(1)` - Status do card (T/F para ativo/bloqueado)
- **board_column_id**: `BIGINT NOT NULL` - Foreign Key para BOARDS_COLUMN

#### 4. BLOCKS
- **id**: `BIGINT AUTO_INCREMENT PRIMARY KEY`
- **block_reason**: `VARCHAR(255) NOT NULL` - Motivo do bloqueio
- **blocked_at**: `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` - Data do bloqueio
- **unblocked_reason**: `VARCHAR(255) NOT NULL` - Motivo do desbloqueio
- **unblock_at**: `TIMESTAMP NULL` - Data do desbloqueio
- **card_id**: `BIGINT NOT NULL` - Foreign Key para CARDS

---

### Requisitos funcionais (sistema)
1. O código deve iniciar disponibilizando um menu com as seguintes opções: Criar novo board, Selecionar board, Excluir boards, Sair.
2. O código deve salvar o board com as suas informações no banco de dados MySQL.
3. O código deve gerar um relatório do board selecionado com o tempo que cada tarefa demorou para ser concluída com informações do tempo que levou em cada coluna 
4. O código dever gerar um relatório do board selecionado com o os bloqueios dos cards, com o tempo que ficaram bloqueados e com a justificativa dos bloqueios e desbloqueios.

---

### Requisitos Não funcionais
1. Cada *Board* terá somente uma coluna do tipo *inicializada*.
2. Se um *Card* estiver marcado como bloqueado ele não pode ser movido até ser desbloqueado.
3. Só poderá alterar a posição do card seguindo o exemplo da sessão acima (Ações).
4. Um card deve armazenar a data e hora em que foi colocado numa coluna e a data e hora que foi movido para a próxima coluna;


## Regras dos boards
    1 - Um board deve ter um nome e ser composto por pelo menos 3 colunas ( coluna onde o card é colocado inicialmente, coluna para cards com tarefas concluídas e coluna para cards cancelados, a nomenclatura das colunas é de escolha livre);
    2 - As colunas tem seu respectivo nome, ordem que aparece no board e seu tipo (Inicial, cancelamento, final e pendente);
    3 - Cada board só pode ter 1 coluna do tipo inicial, cancelamento e final, colunas do tipo pendente podem ter quantas forem necessárias, obrigatoriamente a coluna inicial deve ser a primeira coluna do board, a final deve ser a penúltima e a de cancelamento deve ser a última
    4 - As colunas podem ter 0 ou N cards, cada card tem o seu título, descrição, data de criação e se está bloqueado;
    5 - Um card deve navegar nas colunas seguindo a ordem delas no board, sem pular nenhuma etapa, exceto pela coluna de cards cancelados que pode receber cards diretamente de qualquer coluna que não for a coluna final;
    6 - Se um card estiver marcado como bloqueado ele não pode ser movido até ser desbloqueado
    7 - Para bloquear um card deve-se informar o motivo de seu bloqueio e para desbloquea-lo deve-se também informar o motivo