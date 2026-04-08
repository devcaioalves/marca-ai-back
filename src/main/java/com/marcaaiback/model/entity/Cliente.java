package com.marcaaiback.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "tb_cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 30, unique = true)
    private String telefone;

    @OneToMany(mappedBy = "cliente",  fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos;
}
