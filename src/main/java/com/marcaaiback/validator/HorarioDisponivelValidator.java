package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HorarioDisponivelValidator {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final AgendamentoRepository agendamentoRepository;

    public void validarHorario(HorarioDisponivel horarioDisponivel) {
        if(horarioDisponivel.getData() == null ||
                horarioDisponivel.getHoraInicioExpediente() == null ||
                horarioDisponivel.getHoraFimExpediente() == null){
            throw new OperacaoNaoPermitidaException("A data ou os horários não podem ser nulos.");
        }

        if(horarioDisponivel.getHoraInicioExpediente().isAfter(horarioDisponivel.getHoraFimExpediente())){
            throw new OperacaoNaoPermitidaException("A hora do início do expediente precisa ser menor que a hora do fim do expediente.");
        }

        if(horarioDisponivel.getHoraInicioExpediente().equals(horarioDisponivel.getHoraFimExpediente())){
            throw new OperacaoNaoPermitidaException("Os horários não podem ser iguais.");
        }
    }

    public void validarSobreposicaoHorario(HorarioDisponivel horarioDisponivel, List<HorarioDisponivel> horariosExistentes) {
        for(HorarioDisponivel existente : horariosExistentes){
            boolean temConflito = horarioDisponivel.getHoraInicioExpediente().isBefore(existente.getHoraFimExpediente()) &&
                    horarioDisponivel.getHoraFimExpediente().isAfter(existente.getHoraInicioExpediente());

            if(temConflito){
                throw new OperacaoNaoPermitidaException("Horário sobreposto com outro já cadastrado.");
            }
        }
    }

    public List<LocalTime> validarHorarioParaAgendamento(LocalDate data, Servico servico){
        if(data == null){
            throw new OperacaoNaoPermitidaException("A data está inválida.");
        }

        if(servico.getDuracao() == null ||  servico.getDuracao() <= 0){
            throw new OperacaoNaoPermitidaException("A duração do serviço está inválida.");
        }

        List<HorarioDisponivel> horariosDoDia = horarioDisponivelRepository.findAllByData(data);
        if(horariosDoDia.isEmpty()){
            throw new OperacaoNaoPermitidaException("Não há horários disponíveis para essa data.");
        }

        List<Agendamento> agendamentosDoDia = agendamentoRepository.findAllByDataAgendamento(data);

        List<LocalTime> horariosDisponiveis = new ArrayList<>();

        for(HorarioDisponivel horario:  horariosDoDia){
            LocalTime horarioAtual = horario.getHoraInicioExpediente();
            LocalTime horarioFim = horario.getHoraFimExpediente();

            long duracaoEmMinutos = Math.round(servico.getDuracao());

            while (!horarioAtual.plusMinutes(duracaoEmMinutos).isAfter(horarioFim)) {

                LocalTime fimDoSlot = horarioAtual.plusMinutes(duracaoEmMinutos);

                LocalTime finalHorarioAtual = horarioAtual;
                boolean conflito = agendamentosDoDia.stream().anyMatch(agendamento -> {
                    HorarioDisponivel horarioAgendamento = agendamento.getHorarioDisponivel();
                    if(horarioAgendamento == null
                    || horarioAgendamento.getHoraInicioExpediente() == null
                    || horarioAgendamento.getHoraFimExpediente() == null){
                        return false;
                    }

                    LocalTime inicioAgendamento = horarioAgendamento.getHoraInicioExpediente();
                    LocalTime fimAgendamento = horarioAgendamento.getHoraFimExpediente();

                    return finalHorarioAtual.isBefore(fimAgendamento) && fimDoSlot.isAfter(inicioAgendamento);
                });

                if(!conflito){
                    horariosDisponiveis.add(horarioAtual);
                }

                horarioAtual = horarioAtual.plusMinutes(duracaoEmMinutos);
            }
        }

        return horariosDisponiveis;
    }


}
