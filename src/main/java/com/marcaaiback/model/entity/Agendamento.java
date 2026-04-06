package com.marcaaiback.model.entity;

import com.marcaaiback.model.enuns.StatusAgendamento;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
@Table(name = "tb_agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento statusAgendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servico")
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horarioDisponivel")
    private HorarioDisponivel horarioDisponivel;

    @Column(nullable = false)
    private LocalTime horaInicio;

    // CALCULADO PELO SISTEMA -> HORA_FIM = HORA_INICIO + DURACAO DO SERVIÇO
    @Column(nullable = false)
    private LocalTime horaFim;
}
