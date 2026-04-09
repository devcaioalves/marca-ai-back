package com.marcaaiback.model.dto.agendamento;

import com.marcaaiback.model.enuns.StatusAgendamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgendamentoResponse {

    private Long id;
    private StatusAgendamento statusAgendamento;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    // apenas dados necessários do cliente, sem expor a entidade
    private Long clienteId;
    private String clienteNome;

    // apenas dados necessários do serviço
    private Long servicoId;
    private String servicoNome;

    // apenas dados necessários do horário
    private Long horarioDisponivelId;
}
