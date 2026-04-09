package com.marcaaiback.model.dto.horariodisponivel;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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
public class HorarioDisponivelRequest {

    @NotNull(message = "A data é obrigatória.")
    @FutureOrPresent(message = "A data deve ser no presente ou futuro.")
    private LocalDate data;

    @NotNull(message = "O horário de início é obrigatório.")
    private LocalTime horaInicio;

    @NotNull(message = "O horário de fim é obrigatório.")
    private LocalTime horaFim;
}