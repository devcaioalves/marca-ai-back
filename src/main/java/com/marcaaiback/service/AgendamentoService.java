package com.marcaaiback.service;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoNaoEncontradoException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.AgendamentoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final AgendamentoValidator agendamentoValidator;

    public Agendamento buscarAgendamentoPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
    }

    public List<Agendamento> listarAgendamentosPorClienteOuPorData(Cliente cliente, LocalDate data){
        if(cliente != null &&  data != null){
            return agendamentoRepository.findByClienteAndDataAgendamento(cliente, data);
        }

        if(cliente != null){
            return agendamentoRepository.findAllByCliente(cliente);
        }

        if(data != null){
            return agendamentoRepository.findAllByDataAgendamento(data);
        }

        return agendamentoRepository.findAll();
    }

    public List<Agendamento> listarAgendamentosAgendados(){
        return agendamentoRepository.findAllAgendamentosByStatusAgendamento(StatusAgendamento.AGENDADO);
    }

    public List<Agendamento> listarAgendamentosOrdenados(){
        return agendamentoRepository.findAllByOrderByDataAgendamentoAscHoraInicioAsc();
    }

    public List<Agendamento> listarAgendamentosPorDia(LocalDate data){
        return agendamentoRepository.findByDataAgendamento(data);
    }

    public Agendamento realizarAgendamento(Cliente cliente, Servico servico, LocalTime horaInicio, LocalDate dataAgendamento){
        agendamentoValidator.validarAgendamento(cliente, servico, horaInicio, dataAgendamento);
        agendamentoValidator.validarSeExisteHorarioNoDia(dataAgendamento);
        LocalTime horaFim = calcularHorario(horaInicio, servico);
        agendamentoValidator.validarAntecedencia(dataAgendamento, horaInicio);
        agendamentoValidator.validarHorario(horaInicio, horaFim, dataAgendamento);
        agendamentoValidator.validarConflito(dataAgendamento, horaInicio, horaFim);

        HorarioDisponivel horarioDisponivel = horarioDisponivelRepository
                .findAllByData(dataAgendamento)
                .stream()
                .filter(horario ->
                        (horaInicio.equals(horario.getHoraInicioExpediente()) ||
                        horaInicio.isAfter(horario.getHoraInicioExpediente())) &&
                                (horaFim.equals(horario.getHoraFimExpediente()) || horaFim.isBefore(horario.getHoraFimExpediente()))
                )
                .findFirst()
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Horário não encontrado."));

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataAgendamento(dataAgendamento);
        agendamento.setHoraInicio(horaInicio);
        agendamento.setHoraFim(horaFim);
        agendamento.setHorarioDisponivel(horarioDisponivel);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);

        return agendamentoRepository.save(agendamento);
    }

    public Agendamento remarcarAgendamento(Long id, LocalDate novaData, LocalTime novaHoraInicio){
        Agendamento agendamentoExistente = buscarAgendamentoPorId(id);

        if(agendamentoExistente.getStatusAgendamento().equals(StatusAgendamento.CANCELADO) ||
                agendamentoExistente.getStatusAgendamento().equals(StatusAgendamento.REALIZADO)) {
            throw new OperacaoNaoPermitidaException("Agendamento já realizado ou já cancelado.");
        }

        agendamentoValidator.validarAgendamento(
                agendamentoExistente.getCliente(),
                agendamentoExistente.getServico(), novaHoraInicio,
                novaData);
        agendamentoValidator.validarSeExisteHorarioNoDia(novaData);

        LocalTime horaFim =  calcularHorario(novaHoraInicio, agendamentoExistente.getServico());
        agendamentoValidator.validarAntecedencia(novaData, novaHoraInicio);
        agendamentoValidator.validarHorario(novaHoraInicio, horaFim, novaData);

        List<Agendamento> agendamentosDoDia = agendamentoRepository.findAllByDataAgendamento(novaData);
        for(Agendamento agendamento : agendamentosDoDia) {
            if(agendamento.getId().equals(agendamentoExistente.getId())) {
                continue;
            }
            if(agendamento.getStatusAgendamento().equals(StatusAgendamento.CANCELADO)) {
                continue;
            }

            LocalTime inicioExistente = agendamento.getHoraInicio();
            LocalTime fimExistente = agendamento.getHoraFim();

            boolean temConflito = novaHoraInicio.isBefore(fimExistente) &&
                    horaFim.isAfter(inicioExistente);
            if(temConflito) {
                throw new OperacaoNaoPermitidaException("Já existe agendamentos nesse horário.");
            }
        }

        HorarioDisponivel horarioDisponivel = horarioDisponivelRepository
                .findAllByData(novaData)
                .stream()
                .filter(horario ->
                (novaHoraInicio.equals(horario.getHoraInicioExpediente()) ||
                        novaHoraInicio.isAfter(horario.getHoraInicioExpediente())) &&
                        (horaFim.equals(horario.getHoraFimExpediente()) || horaFim.isBefore(horario.getHoraFimExpediente()))
                )
                .findFirst()
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Horário não encontrado."));

        agendamentoExistente.setDataAgendamento(novaData);
        agendamentoExistente.setHoraInicio(novaHoraInicio);
        agendamentoExistente.setHoraFim(horaFim);
        agendamentoExistente.setHorarioDisponivel(horarioDisponivel);

        return agendamentoRepository.save(agendamentoExistente);
    }

    public Agendamento cancelarAgendamento(Long id) {
        Agendamento agendamentoExistente = buscarAgendamentoPorId(id);

        if(agendamentoExistente.getStatusAgendamento().equals(StatusAgendamento.CANCELADO) ||
                agendamentoExistente.getStatusAgendamento().equals(StatusAgendamento.REALIZADO)) {
            throw new OperacaoNaoPermitidaException("Agendamento já realizado ou já cancelado.");
        }

        LocalDateTime dataHoraAgendamento = LocalDateTime.of(
                agendamentoExistente.getDataAgendamento(),
                agendamentoExistente.getHoraInicio()
        );
        LocalDateTime horaAtual = LocalDateTime.now();

        if(horaAtual.isAfter(dataHoraAgendamento)) {
            throw new OperacaoNaoPermitidaException("Não é possível cancelar o agendamento.");
        }

        LocalDateTime limite = dataHoraAgendamento.minusMinutes(60);

        if(horaAtual.isAfter(limite)){
            throw new OperacaoNaoPermitidaException("O cancelamento deve ser feito com pelo menos 1 hora de antecedência.");
        }

        agendamentoExistente.setStatusAgendamento(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamentoExistente);
    }

    public Agendamento finalizarAgendamento(Long id) {
        Agendamento agendamentoExistente = buscarAgendamentoPorId(id);

        if(agendamentoExistente.getStatusAgendamento().equals(StatusAgendamento.CANCELADO) ||
                agendamentoExistente.getStatusAgendamento().equals(StatusAgendamento.REALIZADO)) {
            throw new OperacaoNaoPermitidaException("Agendamento já realizado ou já cancelado.");
        }

        LocalDateTime dataHoraAtual = LocalDateTime.now();
        LocalDateTime dataHoraAgendamento = LocalDateTime.of(
                agendamentoExistente.getDataAgendamento(),
                agendamentoExistente.getHoraInicio()
        );
        if(dataHoraAtual.isBefore(dataHoraAgendamento)) {
            throw new OperacaoNaoPermitidaException("Não é possível finalizar um agendamento antes do horário.");
        }
        agendamentoExistente.setStatusAgendamento(StatusAgendamento.REALIZADO);
        return agendamentoRepository.save(agendamentoExistente);
    }

    private LocalTime calcularHorario(LocalTime horaInicioAgendamento, Servico servico){
        long duracaoEmMinutos = servico.getDuracao().longValue();
        return horaInicioAgendamento.plusMinutes(duracaoEmMinutos);
    }
}
