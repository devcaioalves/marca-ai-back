package com.marcaaiback.service;

import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.validator.AgendamentoValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final HorarioDisponivelService horarioDisponivelService;
    private final AdminService adminService;
    private final AgendamentoValidator agendamentoValidator;

    public AgendamentoResponse criar(AgendamentoRequest request) {

        Cliente cliente = clienteService.buscarEntidade(request.getClienteId());
        Servico servico = servicoService.buscarEntidade(request.getServicoId());
        HorarioDisponivel horario = horarioDisponivelService.buscarEntidade(request.getHorarioDisponivelId());
        Admin admin = adminService.buscarEntidade();

        LocalTime horaInicio = request.getHoraInicio();
        LocalTime horaFim = horaInicio
                .plusMinutes(servico.getDuracao());

        // validações centralizadas
        agendamentoValidator.validarHorarioDentroDoIntervalo(horario, horaInicio, horaFim);
        agendamentoValidator.validarConflito(horario.getData(), horaInicio, horaFim);
        agendamentoValidator.validarAntecedenciaMinima(horario.getData(), horaInicio);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setHorarioDisponivel(horario);
        agendamento.setAdmin(admin);
        agendamento.setData(horario.getData());
        agendamento.setHoraInicio(horaInicio);
        agendamento.setHoraFim(horaFim);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);

        horarioDisponivelService.salvar(horario);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    public AgendamentoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public List<AgendamentoResponse> listarPorData(LocalDate data) {
        return agendamentoRepository.findByData(data)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponse> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponse> listarPorStatus(StatusAgendamento status) {
        return agendamentoRepository.findByStatusAgendamento(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AgendamentoResponse remarcar(Long id, Long novoHorarioId) {
        Agendamento agendamento = buscarEntidade(id);
        agendamentoValidator.validarRemarcacao(agendamento);

        HorarioDisponivel novoHorario = horarioDisponivelService.buscarEntidade(id);

        Servico servico = agendamento.getServico();

        LocalTime novaHoraInicio = novoHorario.getHoraInicio();
        LocalTime novaHoraFim = novaHoraInicio.plusMinutes(servico.getDuracao());

        agendamentoValidator.validarHorarioDentroDoIntervalo(novoHorario, novaHoraInicio, novaHoraFim);
        agendamentoValidator.validarConflito(novoHorario.getData(), novaHoraInicio, novaHoraFim);
        agendamentoValidator.validarAntecedenciaMinima(novoHorario.getData(), novaHoraInicio);

        agendamento.setHorarioDisponivel(novoHorario);
        agendamento.setData(novoHorario.getData());
        agendamento.setHoraInicio(novaHoraInicio);
        agendamento.setHoraFim(novaHoraFim);
        agendamento.setStatusAgendamento(StatusAgendamento.REMARCADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    public AgendamentoResponse confirmar(Long id){
        Agendamento agendamento = buscarEntidade(id);
        agendamentoValidator.validarConfirmacao(agendamento);
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }


    public AgendamentoResponse cancelar(Long id) {
        Agendamento agendamento = buscarEntidade(id);
        agendamentoValidator.validarCancelamento(agendamento);
        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    public AgendamentoResponse realizarAtendimento(Long id) {
        Agendamento agendamento = buscarEntidade(id);
        agendamentoValidator.validarRealizacao(agendamento);
        agendamento.setStatusAgendamento(StatusAgendamento.REALIZADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    public Agendamento buscarEntidade(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));
    }

    private AgendamentoResponse toResponse(Agendamento agendamento) {
        AgendamentoResponse response = new AgendamentoResponse();
        response.setId(agendamento.getId());
        response.setStatusAgendamento(agendamento.getStatusAgendamento());
        response.setData(agendamento.getData());
        response.setHoraInicio(agendamento.getHoraInicio());
        response.setHoraFim(agendamento.getHoraFim());
        response.setClienteId(agendamento.getCliente().getId());
        response.setClienteNome(agendamento.getCliente().getNome());
        response.setServicoId(agendamento.getServico().getId());
        response.setServicoNome(agendamento.getServico().getNome());
        response.setHorarioDisponivelId(agendamento.getHorarioDisponivel().getId());
        return response;
    }
}