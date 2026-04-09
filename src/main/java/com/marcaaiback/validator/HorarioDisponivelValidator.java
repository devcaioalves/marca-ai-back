package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class HorarioDisponivelValidator {

    private final HorarioDisponivelRepository horarioDisponivelRepository;

    public void validarDuplicidade(LocalDate data, LocalTime horaInicio) {
        if (horarioDisponivelRepository.existsByDataAndHoraInicio(data, horaInicio)) {
            throw new OperacaoNaoPermitidaException(
                    "Já existe um horário cadastrado nessa data e hora."
            );
        }
    }

    public void validarIntervalo(LocalTime horaInicio, LocalTime horaFim) {
        if (horaFim.isBefore(horaInicio) || horaFim.equals(horaInicio)) {
            throw new OperacaoNaoPermitidaException(
                    "A hora fim deve ser maior que a hora início."
            );
        }
    }

    public void validarExclusao(HorarioDisponivel horario) {
        if (!horario.isDisponivel()) {
            throw new OperacaoNaoPermitidaException(
                    "Não é possível deletar um horário que já está vinculado a um agendamento."
            );
        }
    }
}