package com.marcaaiback.model.entity;

import com.marcaaiback.model.enuns.EstadoConversa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_conversa_whatsapp")
public class ConversaWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConversa estadoConversa;

    private Long servicoId;

    private Long horarioId;

    private Long agendamentoId;

    private LocalDate dataEscolhida;

    private LocalTime horaEscolhida;

    private LocalTime horaInicioEscolhida;

    private LocalTime horaFimEscolhida;

    @Column(nullable = false)
    private LocalDateTime ultimaInteracao;

    private Boolean atendimentoHumano;

    private Boolean ativa;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
}