package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.dto.agendamento.AgendamentoResumoResponse;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelRequest;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.HorarioDisponivelValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final AdminService adminService;
    private final HorarioDisponivelValidator horarioValidator;

    public HorarioDisponivelResponse criar(HorarioDisponivelRequest request) {

        // validações centralizadas
        horarioValidator.validarDuplicidade(request.getData(), request.getHoraInicio());
        horarioValidator.validarIntervalo(request.getHoraInicio(), request.getHoraFim());
        horarioValidator.validarHorarioPassado(request.getData(), request.getHoraInicio());
        horarioValidator.validarAntecedencia(request.getData());
        horarioValidator.validarConflitoHorario(request.getData(), request.getHoraInicio(), request.getHoraFim());

        Admin admin = adminService.buscarEntidade();

        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setData(request.getData());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFim(request.getHoraFim());
        horario.setDisponivel(true);
        horario.setAdmin(admin);

        return toResponse(horarioDisponivelRepository.save(horario));
    }

    public HorarioDisponivelResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public List<HorarioDisponivelResponse> listarTodos(){
        return horarioDisponivelRepository.findAllByOrderByDataAscHoraInicioAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HorarioDisponivelResponse> listarPorData(LocalDate data) {

        List<HorarioDisponivel> horarios = horarioDisponivelRepository.findByDataWithAgendamentos(data);

        if (horarios.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum horário disponível encontrado para essa data.");
        }

        return horarios.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<HorarioDisponivelResponse> listarDisponiveisPorData(LocalDate data) {

        List<HorarioDisponivel> horarios = horarioDisponivelRepository.findByDataWithAgendamentos(data);

        if (horarios.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum horário encontrado para essa data.");
        }

        List<HorarioDisponivelResponse> resultado = new ArrayList<>();

        for (HorarioDisponivel horario : horarios) {

            List<Agendamento> agendamentos = horario.getAgendamentos().stream()
                    .filter(a -> a.getStatusAgendamento() == StatusAgendamento.AGENDADO
                            || a.getStatusAgendamento() == StatusAgendamento.CONFIRMADO)
                    .sorted(Comparator.comparing(Agendamento::getHoraInicio))
                    .toList();

            LocalTime inicioLivre = horario.getHoraInicio();

            for (Agendamento agendamento : agendamentos) {

                LocalTime inicioAg = agendamento.getHoraInicio();
                LocalTime fimAg = agendamento.getHoraFim();

                if (inicioLivre.isBefore(inicioAg)) {

                    long minutos = Duration.between(inicioLivre, inicioAg).toMinutes();

                    if (minutos >= 30) {
                        resultado.add(criarIntervaloResponse(horario, inicioLivre, inicioAg));
                    }
                }

                if (inicioLivre.isBefore(fimAg)) {
                    inicioLivre = fimAg;
                }
            }

            if (inicioLivre.isBefore(horario.getHoraFim())) {

                long minutos = Duration.between(inicioLivre, horario.getHoraFim()).toMinutes();

                if (minutos >= 30) {
                    resultado.add(criarIntervaloResponse(horario, inicioLivre, horario.getHoraFim()));
                }
            }
        }

        if (resultado.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum horário disponível para essa data.");
        }

        return resultado;
    }

    public HorarioDisponivelResponse alterarDisponibilidade(Long id, HorarioDisponivelRequest request) {

        HorarioDisponivel horario = buscarEntidade(id);

        // validações básicas
        horarioValidator.validarDuplicidadeAtualizacao(id, request.getData(), request.getHoraInicio());
        horarioValidator.validarIntervalo(request.getHoraInicio(), request.getHoraFim());
        horarioValidator.validarHorarioPassado(request.getData(), request.getHoraInicio());
        horarioValidator.validarAntecedencia(request.getData());
        horarioValidator.validarConflitoAtualizacao(id, request.getData(), request.getHoraInicio(), request.getHoraFim());

        // 🔥 NOVA VALIDAÇÃO INTELIGENTE
        for (Agendamento a : horario.getAgendamentos()) {

            if (a.getStatusAgendamento() == StatusAgendamento.AGENDADO ||
                    a.getStatusAgendamento() == StatusAgendamento.CONFIRMADO) {

                if (a.getHoraInicio().isBefore(request.getHoraInicio()) ||
                        a.getHoraFim().isAfter(request.getHoraFim())) {

                    throw new OperacaoNaoPermitidaException(
                            "Não é possível alterar o horário pois existem agendamentos fora do novo intervalo."
                    );
                }
            }
        }

        horario.setData(request.getData());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFim(request.getHoraFim());

        return toResponse(horarioDisponivelRepository.save(horario));
    }

    public HorarioDisponivel salvar(HorarioDisponivel horario){
        return  horarioDisponivelRepository.save(horario);
    }


    public void deletar(Long id) {
        HorarioDisponivel horario = buscarEntidade(id);

        // validação antes de deletar
        horarioValidator.validarExclusao(horario);

        horarioDisponivelRepository.delete(horario);
    }

    // método interno reutilizável pelos outros services
    public HorarioDisponivel buscarEntidade(Long id) {
        return horarioDisponivelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horário não encontrado."));
    }

    private HorarioDisponivelResponse toResponse(HorarioDisponivel horario) {
        HorarioDisponivelResponse response = new HorarioDisponivelResponse();
        response.setId(horario.getId());
        response.setData(horario.getData());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFim(horario.getHoraFim());
        response.setDisponivel(horario.isDisponivel());

        List<AgendamentoResumoResponse> agendamentos = horario.getAgendamentos() == null
                ? List.of()
                : horario.getAgendamentos().stream().map(a -> {
            AgendamentoResumoResponse resumo = new AgendamentoResumoResponse();
            resumo.setId(a.getId());
            resumo.setData(a.getData());
            resumo.setHoraInicio(a.getHoraInicio());
            resumo.setHoraFim(a.getHoraFim());
            resumo.setStatusAgendamento(a.getStatusAgendamento());
            resumo.setServicoNome(a.getServico().getNome());
            resumo.setClienteNome(a.getCliente().getNome());
            return resumo;
        }).collect(Collectors.toList());

        response.setAgendamentos(agendamentos);
        return response;
    }

    private HorarioDisponivelResponse criarIntervaloResponse(
            HorarioDisponivel base,
            LocalTime inicio,
            LocalTime fim
    ) {
        HorarioDisponivelResponse response = new HorarioDisponivelResponse();

        response.setData(base.getData());
        response.setHoraInicio(inicio);
        response.setHoraFim(fim);
        response.setDisponivel(true);
        response.setAgendamentos(List.of()); // aqui não precisa trazer agendamentos

        return response;
    }
}