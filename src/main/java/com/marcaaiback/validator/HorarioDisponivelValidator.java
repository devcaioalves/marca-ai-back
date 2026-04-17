package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HorarioDisponivelValidator {

    private final HorarioDisponivelRepository horarioDisponivelRepository;

    public void validarDuplicidade(LocalDate data, LocalTime horaInicio) {
        if (horarioDisponivelRepository.existsByDataAndHoraInicio(data, horaInicio)) {
            throw new RecursoDuplicadoException(
                    "Já existe um horário cadastrado nessa data e hora."
            );
        }
    }

    public void validarDuplicidadeAtualizacao(Long id, LocalDate data, LocalTime horaInicio) {
        Optional<HorarioDisponivel> horarios = horarioDisponivelRepository.findByDataAndHoraInicio(data, horaInicio);

        if(horarios.isPresent() && !horarios.get().getId().equals(id)) {
            throw new RecursoDuplicadoException("Já existe um horário cadastrado nessa data e hora.");
        }
    }

    public void validarIntervalo(LocalTime horaInicio, LocalTime horaFim) {
        if (horaFim.isBefore(horaInicio) || horaFim.equals(horaInicio)) {
            throw new OperacaoNaoPermitidaException(
                    "A hora fim deve ser maior que a hora início."
            );
        }
    }

    public void validarConflitoHorario(LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        List<HorarioDisponivel> horariosDoDia = horarioDisponivelRepository.findByData(data);

        for (HorarioDisponivel horario : horariosDoDia) {
            boolean temConflito = horaInicio.isBefore(horario.getHoraFim()) &&
                    horaFim.isAfter(horario.getHoraInicio());

            if(temConflito) {
                throw new RecursoDuplicadoException("Já existe um horário cadastrado com esse intervalo.");
            }
        }
    }

    public void validarConflitoAtualizacao(Long id, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        List<HorarioDisponivel> horarioDoDia = horarioDisponivelRepository.findByData(data);

        for (HorarioDisponivel horario : horarioDoDia) {
            if(horario.getId().equals(id)) {
                continue;
            }

            boolean temConflito = horaInicio.isBefore(horario.getHoraFim()) &&
                    horaFim.isAfter(horario.getHoraInicio());

            if(temConflito) {
                throw new RecursoDuplicadoException("Já existe um horário cadastrado nessa data e hora.");
            }
        }
    }

    public void validarAntecedencia(LocalDate data){
        LocalDate dataAtual = LocalDate.now();
        LocalDate minimoPermitido = dataAtual.plusDays(1);

        if(data.isBefore(minimoPermitido)) {
            throw new OperacaoNaoPermitidaException("Os horários devem ser cadastrados com pelo menos 1 dia de antecedência.");
        }
    }

    public void validarHorarioPassado(LocalDate data, LocalTime horaInicio) {
        LocalDate dataAtual =  LocalDate.now();
        LocalTime horaAtual = LocalTime.now();

        if(data.equals(dataAtual) && horaInicio.isBefore(horaAtual)) {
            throw new OperacaoNaoPermitidaException("Não é permitido cadastrar horário no passado.");
        }
    }

    public void validarExclusao(HorarioDisponivel horario) {
        boolean possuiAgendamento = horario.getAgendamentos()
                .stream()
                .anyMatch(a -> a.getStatusAgendamento() == StatusAgendamento.AGENDADO ||
                        a.getStatusAgendamento() == StatusAgendamento.CONFIRMADO);
        if(possuiAgendamento) {
            throw new OperacaoNaoPermitidaException("Não é possível excluir este horário, pois existem agendamentos vinculados a ele.");
        }
    }

}