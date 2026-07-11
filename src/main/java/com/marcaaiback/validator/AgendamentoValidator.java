package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AgendamentoValidator {

    private final AgendamentoRepository agendamentoRepository;

    public void validarHorarioDentroDoIntervalo(HorarioDisponivel horario, LocalTime horaInicio, LocalTime horaFim) {
        if(horaInicio.isBefore(horario.getHoraInicio()) ||
            horaFim.isAfter(horario.getHoraFim())){
                throw new OperacaoNaoPermitidaException("Horário fora do intervalo disponível.");
        }
    }

    public void validarCancelamento(Agendamento agendamento) {
        if (agendamento.getStatusAgendamento() == StatusAgendamento.CANCELADO) {
            throw new OperacaoNaoPermitidaException("Agendamento já está cancelado.");
        }

        if(agendamento.getStatusAgendamento() == StatusAgendamento.REALIZADO){
            throw new OperacaoNaoPermitidaException("Agendamentos realizados não podem ser cancelados.");
        }

        LocalDateTime dataHoraAgendamento = LocalDateTime.of(agendamento.getData(), agendamento.getHoraInicio());
        LocalDateTime dataHoraAtual = LocalDateTime.now();

        if(dataHoraAgendamento.isBefore(dataHoraAtual.plusMinutes(60))){
            throw new OperacaoNaoPermitidaException("O cancelamento deve ser feito com pelo menos 1 hora de antecedência");
        }
    }

    public void validarRealizacao(Agendamento agendamento) {
        if (agendamento.getStatusAgendamento() != StatusAgendamento.CONFIRMADO) {
            throw new OperacaoNaoPermitidaException("Apenas agendamentos confirmados podem ser realizados.");
        }
    }

    public void validarRemarcacao(Agendamento agendamento){
        if (agendamento.getStatusAgendamento() == StatusAgendamento.CANCELADO ||
            agendamento.getStatusAgendamento() == StatusAgendamento.REALIZADO) {
            throw new OperacaoNaoPermitidaException("Agendamentos cancelados ou realizados não podem ser remarcados.");
        }

        LocalDateTime dataHoraAgendamento = LocalDateTime.of(agendamento.getData(), agendamento.getHoraInicio());

        if(dataHoraAgendamento.isBefore(LocalDateTime.now())){
            throw new OperacaoNaoPermitidaException("Não é possível remarcar um agendamento cujo horário já passou.");
        }
    }

    public void validarConfirmacao(Agendamento agendamento){
        if(agendamento.getStatusAgendamento() == StatusAgendamento.CANCELADO){
            throw new OperacaoNaoPermitidaException("Agendamentos cancelados não podem ser confirmados.");
        }

        if(agendamento.getStatusAgendamento() == StatusAgendamento.REALIZADO){
            throw new OperacaoNaoPermitidaException("Agendamentos realizados não podem ser confirmados.");
        }

        if(agendamento.getStatusAgendamento() == StatusAgendamento.CONFIRMADO){
            throw new RecursoDuplicadoException("O agendamento já está confirmado.");
        }
    }

    public void validarAntecedenciaMinima(LocalDate data, LocalTime horaInicio) {
        LocalDateTime dataHoraAgendamento = LocalDateTime.of(data, horaInicio);
        LocalDateTime dataHoraAtual =  LocalDateTime.now();

        LocalDateTime limiteMinimo = dataHoraAtual.plusMinutes(30);

        if(dataHoraAgendamento.isBefore(limiteMinimo)){
            throw new OperacaoNaoPermitidaException("O agendamento deve ser feito com pelo menos 30 minutos de antecedência.");
        }
    }

    public void validarConflito(HorarioDisponivel horario, LocalTime horaInicio, LocalTime horaFim, Long agendamentoIgnorar) {
        List<Agendamento> agendamentos = horario.getAgendamentos();

        for (Agendamento agendamento : agendamentos) {

            if(agendamentoIgnorar != null && agendamento.getId().equals(agendamentoIgnorar)){
                continue;
            }

            if (agendamento.getStatusAgendamento() == StatusAgendamento.CANCELADO) {
                continue;
            }

            boolean conflito = horaInicio.isBefore(agendamento.getHoraFim()) &&
                    horaFim.isAfter(agendamento.getHoraInicio());

            if (conflito) {
                throw new OperacaoNaoPermitidaException("Horário indisponível.");
            }
        }
    }

    public void validarConflito(HorarioDisponivel horario, LocalTime horaInicio, LocalTime horaFim) {
        validarConflito(horario, horaInicio, horaFim, null);
    }

    public void validarServicoAtivo(Servico servico) {
        if (!servico.isAtivo()) {
            throw new OperacaoNaoPermitidaException("Não é possível agendar um serviço desativado.");
        }
    }

    public StatusAgendamento definirStatusInicial(LocalDate data, LocalTime horaInicio) {
        LocalDateTime dataHoraAgendamento = LocalDateTime.of(data, horaInicio);
        LocalDateTime dataHoraAtual =  LocalDateTime.now();

        long minutos = Duration.between(dataHoraAtual, dataHoraAgendamento).toMinutes();

        if(minutos < 240){
            return StatusAgendamento.CONFIRMADO;
        }

        return StatusAgendamento.AGENDADO;
    }
}