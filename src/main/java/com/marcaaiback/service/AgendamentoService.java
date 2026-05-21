package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.admin.EnderecoResponse;
import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.AgendamentoValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    private final HorarioDisponivelRepository horarioRepository;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final AdminService adminService;
    private final AgendamentoValidator agendamentoValidator;

    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {

        Cliente cliente = clienteService.buscarEntidade(request.getClienteId());
        Servico servico = servicoService.buscarEntidade(request.getServicoId());

        HorarioDisponivel horario = horarioRepository.buscarComLock(request.getHorarioDisponivelId())
                .orElseThrow(() -> new EntityNotFoundException("Horário não encontrado"));

        Admin admin = adminService.buscarEntidade();

        LocalTime horaInicio = request.getHoraInicio();
        LocalTime horaFim = calcularHoraFim(horaInicio, servico);

        // 🔥 VALIDAÇÃO REAL
        agendamentoValidator.validarHorarioDentroDoIntervalo(horario, horaInicio, horaFim);
        agendamentoValidator.validarConflito(horario, horaInicio, horaFim); // ALTERADO
        agendamentoValidator.validarAntecedenciaMinima(horario.getData(), horaInicio);
        agendamentoValidator.validarServicoAtivo(servico);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setHorarioDisponivel(horario);
        agendamento.setAdmin(admin);
        agendamento.setData(horario.getData());
        agendamento.setHoraInicio(horaInicio);
        agendamento.setHoraFim(horaFim);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    public AgendamentoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public List<AgendamentoResponse> listarTodos() {
        List<Agendamento> agendamentos = agendamentoRepository.findAll();

        if (agendamentos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum agendamento disponível encontrado.");
        }

        return agendamentos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponse> listarPorData(LocalDate data) {
        List<Agendamento> agendamentos = agendamentoRepository.findByData(data);

        if (agendamentos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum agendamento disponível encontrado para essa data.");
        }

        return agendamentos.stream()
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

    public AgendamentoResponse confirmar(Long id){
        Agendamento agendamento = buscarEntidade(id);
        agendamentoValidator.validarConfirmacao(agendamento);
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    public AgendamentoResponse remarcar(Long id, Long novoHorarioId) {

        Agendamento agendamento = buscarEntidade(id);
        agendamentoValidator.validarRemarcacao(agendamento);

        HorarioDisponivel novoHorario = horarioRepository.buscarComLock(novoHorarioId)
                .orElseThrow(() -> new EntityNotFoundException("Horário não encontrado"));

        Servico servico = agendamento.getServico();

        LocalTime novaHoraInicio = novoHorario.getHoraInicio();
        LocalTime novaHoraFim = calcularHoraFim(novaHoraInicio, servico);

        agendamentoValidator.validarHorarioDentroDoIntervalo(novoHorario, novaHoraInicio, novaHoraFim);
        agendamentoValidator.validarConflito(novoHorario, novaHoraInicio, novaHoraFim);
        agendamentoValidator.validarAntecedenciaMinima(novoHorario.getData(), novaHoraInicio);
        agendamentoValidator.validarServicoAtivo(servico);

        agendamento.setHorarioDisponivel(novoHorario);
        agendamento.setData(novoHorario.getData());
        agendamento.setHoraInicio(novaHoraInicio);
        agendamento.setHoraFim(novaHoraFim);
        agendamento.setStatusAgendamento(StatusAgendamento.REMARCADO);

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

    private LocalTime calcularHoraFim(LocalTime horaInicio, Servico servico) {
        return horaInicio.plusMinutes(servico.getDuracao());
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

        Admin admin = agendamento.getHorarioDisponivel().getAdmin();

        if (admin != null && admin.getEndereco() != null) {
            Endereco endereco = admin.getEndereco();

            EnderecoResponse enderecoResponse = new EnderecoResponse();
            enderecoResponse.setRua(endereco.getRua());
            enderecoResponse.setNumero(endereco.getNumero());
            enderecoResponse.setBairro(endereco.getBairro());
            enderecoResponse.setCidade(endereco.getCidade());
            enderecoResponse.setEstado(endereco.getEstado());
            enderecoResponse.setCep(endereco.getCep());

            response.setEndereco(enderecoResponse);
        }

        return response;
    }
}