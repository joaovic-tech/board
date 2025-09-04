package tech.joaovic.persistence.entity;

import lombok.Getter;

/**
 * Enum que representa os tipos de colunas que um board pode ter.
 * Conforme as regras da DIO:
 * - INICIAL: Apenas uma por board, sempre a primeira coluna
 * - PENDENTE: Pode haver várias, ficam entre a inicial e final  
 * - FINAL: Apenas uma por board, sempre a penúltima coluna
 * - CANCELAMENTO: Apenas uma por board, sempre a última coluna
 */
@Getter
public enum BoardColumnTypeEnum {
    INICIAL("Inicial"),
    PENDENTE("Pendente"), 
    FINAL("Final"),
    CANCELAMENTO("Cancelamento");
    
    private final String defaultName;
    
    BoardColumnTypeEnum(String defaultName) {
        this.defaultName = defaultName;
    }
}
