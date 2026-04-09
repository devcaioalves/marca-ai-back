package com.marcaaiback.dto.agendamento;

import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.StatusAgendamento;
import jakarta.validation.constraints.NotBlank;
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
public class AgendamentoCreateDTO {

    private StatusAgendamento statusAgendamento;
    @NotBlank(message = "O agendamento deve conter um cliente.")
    private Cliente cliente;
    @NotBlank(message = "O agendamento deve conter um serviço.")
    private Servico servico;
    @NotBlank(message = "O agendamento deve conter um horário disponível.")
    private HorarioDisponivel horarioDisponivel;
    @NotBlank(message = "O agendamento deve mostrar o horário de início.")
    private LocalTime horaInicio;
    @NotBlank(message = "O agendamento deve mostrar o horário de fim.")
    private LocalTime horaFim;
    @NotBlank(message = "O agendamento deve mostrar sua data.")
    private LocalDate dataAgendamento;
}
