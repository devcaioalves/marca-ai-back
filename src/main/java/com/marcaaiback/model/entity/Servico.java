package com.marcaaiback.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "tb_servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    private String descricao;

    @Column(precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private Double duracao;

    @OneToMany(mappedBy = "servico",  fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos;

    private boolean ativo;
}
