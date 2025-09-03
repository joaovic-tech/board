package tech.joaovic.persistence.entity;

import lombok.Data;

@Data
public class BoardColumnEntity {
    private Long id;
    private BoardColumnNameEnum name;
    private Integer nivel;
}
