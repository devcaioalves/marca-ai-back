package com.marcaaiback.model.dto.horariodisponivel;

import com.marcaaiback.model.dto.agendamento.AgendamentoResumoResponse;
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
public class HorarioDisponivelResponse {

    private Long id;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private boolean disponivel;
    private List<AgendamentoResumoResponse> agendamentos;
}
