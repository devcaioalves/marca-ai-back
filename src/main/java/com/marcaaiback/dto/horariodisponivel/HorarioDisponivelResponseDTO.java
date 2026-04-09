package com.marcaaiback.dto.horariodisponivel;

import com.marcaaiback.model.entity.Agendamento;
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
public class HorarioDisponivelResponseDTO {

    private Long id;
    private LocalDate data;
    private LocalTime horaInicioExpediente;
    private LocalTime horaFimExpediente;
    private List<Agendamento> agendamentos;

}
