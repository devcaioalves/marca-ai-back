package com.marcaaiback.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(nullable = false)
    private boolean disponivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "horarioDisponivel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos;
}
