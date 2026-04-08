package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AgendamentoValidator {

    private final AgendamentoRepository  agendamentoRepository;
    private final HorarioDisponivelRepository horarioDisponivelRepository;

    public void validarAgendamento(Cliente cliente, Servico servico, LocalTime horaInicioAgendamento, LocalDate dataAgendamento){
        if(cliente == null){
            throw new OperacaoNaoPermitidaException("Cliente inválido.");
        }

        if(servico == null){
            throw new OperacaoNaoPermitidaException("Serviço inválido.");
        }

        if(horaInicioAgendamento == null){
            throw new OperacaoNaoPermitidaException("Horário inválido.");
        }

        if(dataAgendamento == null || dataAgendamento.isBefore(LocalDate.now())){
            throw new OperacaoNaoPermitidaException("Data para agendamento inválida.");
        }
    }

    public void validarHorario(LocalTime horaInicioAgendamento, LocalTime horaFimAgendamento, LocalDate dataAgendamento){
        List<HorarioDisponivel> horariosDoDia = horarioDisponivelRepository.findAllByData(dataAgendamento);

        boolean dentroDoHorario = false;
        for(HorarioDisponivel horario : horariosDoDia){
            LocalTime inicioExpediente = horario.getHoraInicioExpediente();
            LocalTime fimExpediente = horario.getHoraFimExpediente();

            if(horaInicioAgendamento != null && horaFimAgendamento != null &&
                    (horaInicioAgendamento.equals(inicioExpediente) || horaInicioAgendamento.isAfter(inicioExpediente)) &&
                    (horaFimAgendamento.equals(fimExpediente) || horaFimAgendamento.isBefore(fimExpediente))){
                    dentroDoHorario = true;
                    break;
            }
        }

        if(!dentroDoHorario){
            throw new OperacaoNaoPermitidaException("Horário fora do expediente");
        }
    }

    public void validarSeExisteHorarioNoDia(LocalDate dataAgendamento){
        boolean horarioExistente = horarioDisponivelRepository.existsByData(dataAgendamento);
        if(!horarioExistente){
            throw new OperacaoNaoPermitidaException("Não existe horário disponível nessa data.");
        }
    }

    public void validarAntecedencia(LocalDate dataAgendamento, LocalTime horaInicioAgendamento){
        LocalDateTime horarioAtual = LocalDateTime.now();
        LocalDateTime dataHoraAgendamento = LocalDateTime.of(dataAgendamento, horaInicioAgendamento);
        LocalDateTime limite = horarioAtual.plusMinutes(30);

        if(dataHoraAgendamento.isBefore(limite)){
            throw new OperacaoNaoPermitidaException("O agendamento deve ter pelo menos 30 minutos de antecedência.");
        }
    }


    public void validarConflito(LocalDate dataAgendamento, LocalTime horaInicio, LocalTime horaFim){
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findAllByDataAgendamento(dataAgendamento);

        boolean temConflito;
        for(Agendamento agendamento : agendamentosDoDia){
            LocalTime inicioExistente = agendamento.getHoraInicio();
            LocalTime fimExistente = agendamento.getHoraFim();

            temConflito = horaInicio.isBefore(fimExistente) &&
                    horaFim.isAfter(inicioExistente);

            if(temConflito){
                throw new OperacaoNaoPermitidaException("Já existe agendamento nesse horário.");
            }
        }
    }
}
