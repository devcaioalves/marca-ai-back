package com.marcaaiback.model.dto.conversa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmacaoAgendamentoResponse {

    private Long agendamentoId;
    private String telefone;
    private String nomeCliente;
    private String servico;
    private LocalDate data;
    private LocalTime horaInicio;
}
