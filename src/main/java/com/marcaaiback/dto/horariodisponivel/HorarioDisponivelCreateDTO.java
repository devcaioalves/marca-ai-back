package com.marcaaiback.dto.horariodisponivel;

import com.marcaaiback.model.entity.Agendamento;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HorarioDisponivelCreateDTO {

    @NotBlank(message = "O horário disponível deve conter a data.")
    private LocalDate data;
    @NotBlank(message = "O horário disponível deve conter a hora de início do expediente.")
    private LocalTime horaInicioExpediente;
    @NotBlank(message = "O horário disponível deve conter a hora de término do expediente.")
    private LocalTime horaFimExpediente;
    @NotBlank(message = "O horário disponível deve conter os agendamentos.")
    private List<Agendamento> agendamentos;

}
