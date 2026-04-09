package com.marcaaiback.model.entity;

import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNotificacao statusNotificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeMensagem tipoDeMensagem;

    // cliente removido - acessado via agendamento.getCliente()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agendamento", nullable = false)
    private Agendamento agendamento;
}