package com.marcaaiback.model.dto.agendamento;

import com.marcaaiback.model.dto.admin.EnderecoResponse;
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
public class AgendamentoResumoResponse {

    private Long id;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusAgendamento statusAgendamento;
    private String servicoNome;
    private String clienteNome;
}
