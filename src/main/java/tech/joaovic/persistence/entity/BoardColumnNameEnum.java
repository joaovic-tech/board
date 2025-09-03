package tech.joaovic.persistence.entity;

import lombok.Getter;

@Getter
public enum BoardColumnNameEnum {
    INITIALIZED("Inicializada"), PENDING("Pendente"), COMPLETED("Concluída"), FINALIZED("Finalizada"), CANCELLED("Cancelada");
    BoardColumnNameEnum(String nameColumn) {}
}
