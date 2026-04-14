package com.marcaaiback.model.dto.agendamento;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgendamentoRequest {

    @NotNull(message = "O agendamento deve conter a hora início.")
    private LocalTime horaInicio;

    @NotNull(message = "O agendamento deve conter um cliente.")
    private Long clienteId;

    @NotNull(message = "O agendamento deve conter um serviço.")
    private Long servicoId;

    @NotNull(message = "O agendamento deve conter um horário disponível.")
    private Long horarioDisponivelId;

}
