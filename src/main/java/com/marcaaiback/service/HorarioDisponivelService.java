package com.marcaaiback.service;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoNaoEncontradoException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.HorarioDisponivelValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final HorarioDisponivelValidator horarioDisponivelValidator;

    // METÓDOS SOBRE HORÁRIOS DE FUNCIONAMENTO DO SALÃO:

    public HorarioDisponivel buscarHorarioPorId(Long id){
        return horarioDisponivelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum horário encontrado."));
    }

    public List<HorarioDisponivel> listarHorarioDeFuncionamentoPorData(LocalDate data){
        return horarioDisponivelRepository.findAllByData(data);
    }

    public HorarioDisponivel cadastrarHorarioDeFuncionamento(HorarioDisponivel horarioDisponivel){
        horarioDisponivelValidator.validarHorario(horarioDisponivel);
        List<HorarioDisponivel> horariosDoDia = horarioDisponivelRepository.findAllByData(horarioDisponivel.getData());
        horarioDisponivelValidator.validarSobreposicaoHorario(horarioDisponivel, horariosDoDia);
        return  horarioDisponivelRepository.save(horarioDisponivel);
    }

    public HorarioDisponivel atualizarHorarioDeFuncionamento(Long id, HorarioDisponivel novoHorario){
        HorarioDisponivel horarioExistente = buscarHorarioPorId(id);

        horarioDisponivelValidator.validarHorario(novoHorario);

        List<HorarioDisponivel> horariosDoDia =  horarioDisponivelRepository.findAllByData(novoHorario.getData())
                .stream()
                .filter(horario -> !horario.getId().equals(id))
                .collect(Collectors.toList());
        horarioDisponivelValidator.validarSobreposicaoHorario(novoHorario, horariosDoDia);

        List<Agendamento> agendamentosDoDia = agendamentoRepository.findAllByDataAgendamento(novoHorario.getData());
        for(Agendamento agendamento : agendamentosDoDia){
            if (agendamento.getHorarioDisponivel() != null && agendamento.getHorarioDisponivel().getId().equals(horarioExistente.getId())){
                throw new OperacaoNaoPermitidaException("Não é possível atualizar esse horário porque já existem agendamentos.");
            }
        }

        horarioExistente.setData(novoHorario.getData());
        horarioExistente.setHoraInicioExpediente(novoHorario.getHoraInicioExpediente());
        horarioExistente.setHoraFimExpediente(novoHorario.getHoraFimExpediente());
        return  horarioDisponivelRepository.save(horarioExistente);
    }

    public void removerHorarioDeFuncionamento(Long id){
        HorarioDisponivel horarioExistente = buscarHorarioPorId(id);

        List<Agendamento> agendamentos = agendamentoRepository.findAllByDataAgendamento(horarioExistente.getData());
        for(Agendamento agendamento : agendamentos){
            if(agendamento.getHorarioDisponivel() != null && agendamento.getHorarioDisponivel().getId().equals(horarioExistente.getId())){
                throw new OperacaoNaoPermitidaException("Não é possível excluir esse horário porque existem agendamentos.");
            }
        }
        horarioDisponivelRepository.deleteById(id);
    }

    // METÓDOS SOBRE HORÁRIOS DISPONÍVEIS PARA AGENDAMENTO:

    public List<String> gerarHorarioDisponivelParaAgendamento(LocalDate data, Servico servico){
        List<LocalTime> horariosDisponiveis = horarioDisponivelValidator.validarHorarioParaAgendamento(data, servico);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<String> horariosFormatados = horariosDisponiveis.stream()
                .map(horario -> horario.format(formatter))
                .collect(Collectors.toList());
        horariosFormatados.sort(Comparator.naturalOrder());
        return horariosFormatados;
    }
}
