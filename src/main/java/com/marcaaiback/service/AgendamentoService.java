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

        // validações centralizadas
        agendamentoValidator.validarHorarioDisponivel(horario);

        LocalTime horaFim = horario.getHoraInicio()
                .plusMinutes(servico.getDuracao());

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setHorarioDisponivel(horario);
        agendamento.setAdmin(admin);
        agendamento.setData(horario.getData());
        agendamento.setHoraInicio(horario.getHoraInicio());
        agendamento.setHoraFim(horaFim);
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        horario.setDisponivel(false);
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

    public AgendamentoResponse cancelar(Long id) {
        Agendamento agendamento = buscarEntidade(id);

        agendamentoValidator.validarCancelamento(agendamento);

        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);

        HorarioDisponivel horario = agendamento.getHorarioDisponivel();
        horario.setDisponivel(true);
        horarioDisponivelService.salvar(horario);

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