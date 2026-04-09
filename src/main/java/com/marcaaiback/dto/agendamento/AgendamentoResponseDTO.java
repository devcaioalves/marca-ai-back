package com.marcaaiback.dto.agendamento;

import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
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
public class AgendamentoResponseDTO {

    private Long id;
    private StatusAgendamento statusAgendamento;
    private Cliente cliente;
    private Servico servico;
    private HorarioDisponivel horarioDisponivel;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private LocalDate dataAgendamento;

}
