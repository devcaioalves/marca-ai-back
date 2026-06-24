package com.marcaaiback.model.dto.conversa;

import com.marcaaiback.model.enuns.EstadoConversa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConversaWhatsappResponse {

    private Long id;

    private EstadoConversa estadoConversa;

    private Long servicoId;

    private Long horarioId;

    private LocalTime horaInicioEscolhida;

    private LocalTime horaFimEscolhida;

    private Long agendamentoId;

    private LocalDate dataEscolhida;

    private LocalDateTime ultimaInteracao;

    private Long clienteId;

    private String clienteNome;

    private String telefoneCliente;
}