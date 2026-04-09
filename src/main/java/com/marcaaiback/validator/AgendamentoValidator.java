package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgendamentoValidator {

    private final AgendamentoRepository agendamentoRepository;

    public void validarHorarioDisponivel(HorarioDisponivel horario) {
        if (!horario.isDisponivel()) {
            throw new OperacaoNaoPermitidaException("Esse horário não está mais disponível.");
        }

        if (agendamentoRepository.existsByHorarioDisponivelId(horario.getId())) {
            throw new OperacaoNaoPermitidaException("Já existe um agendamento nesse horário.");
        }
    }

    public void validarCancelamento(Agendamento agendamento) {
        if (agendamento.getStatusAgendamento() == StatusAgendamento.CANCELADO) {
            throw new OperacaoNaoPermitidaException("Agendamento já está cancelado.");
        }
    }

    public void validarRealizacao(Agendamento agendamento) {
        if (agendamento.getStatusAgendamento() != StatusAgendamento.CONFIRMADO) {
            throw new OperacaoNaoPermitidaException("Apenas agendamentos confirmados podem ser realizados.");
        }
    }
}