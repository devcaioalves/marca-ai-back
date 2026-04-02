package com.marcaaiback.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "tb_horario_disponivel")
public class HorarioDisponivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @OneToMany(mappedBy = "horarioDisponivel",  fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos;

    private boolean disponivel;
}
