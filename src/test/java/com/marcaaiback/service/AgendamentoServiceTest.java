package com.marcaaiback.service;

import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.AgendamentoValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock HorarioDisponivelRepository horarioRepository;
    @Mock ClienteService clienteService;
    @Mock ServicoService servicoService;
    @Mock AdminService adminService;
    @Mock AgendamentoValidator agendamentoValidator;

    @InjectMocks AgendamentoService agendamentoService;

    private Cliente cliente;
    private Servico servico;
    private Admin admin;
    private HorarioDisponivel horario;
    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setTelefone("5581999999999");

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte");
        servico.setDuracao(30);
        servico.setAtivo(true);

        Endereco endereco = new Endereco();
        endereco.setRua("Rua A");
        endereco.setCidade("Recife");

        admin = Admin.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@email.com")
                .telefone("81999999999")
                .senha("encoded")
                .endereco(endereco)
                .build();

        horario = new HorarioDisponivel();
        horario.setId(1L);
        horario.setData(LocalDate.now().plusDays(2));
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFim(LocalTime.of(18, 0));
        horario.setDisponivel(true);
        horario.setAdmin(admin);
        horario.setAgendamentos(new ArrayList<>());

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setHorarioDisponivel(horario);
        agendamento.setAdmin(admin);
        agendamento.setData(horario.getData());
        agendamento.setHoraInicio(LocalTime.of(9, 0));
        agendamento.setHoraFim(LocalTime.of(9, 30));
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);
    }

    // ── criar ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criar: sucesso cria agendamento")
    void criar_sucesso() {
        AgendamentoRequest request = new AgendamentoRequest(LocalTime.of(9, 0), 1L, 1L, 1L);

        when(clienteService.buscarEntidade(1L)).thenReturn(cliente);
        when(servicoService.buscarEntidade(1L)).thenReturn(servico);
        when(horarioRepository.buscarComLock(1L)).thenReturn(Optional.of(horario));
        when(adminService.buscarEntidade()).thenReturn(admin);
        doNothing().when(agendamentoValidator).validarHorarioDentroDoIntervalo(any(), any(), any());
        doNothing().when(agendamentoValidator).validarConflito(any(), any(), any());
        doNothing().when(agendamentoValidator).validarAntecedenciaMinima(any(), any());
        doNothing().when(agendamentoValidator).validarServicoAtivo(any());
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.getClienteId()).isEqualTo(1L);
        verify(agendamentoRepository).save(any());
    }

    @Test
    @DisplayName("criar: lança EntityNotFoundException quando horário não encontrado")
    void criar_horarioNaoEncontrado() {
        AgendamentoRequest request = new AgendamentoRequest(LocalTime.of(9, 0), 1L, 1L, 99L);

        when(clienteService.buscarEntidade(1L)).thenReturn(cliente);
        when(servicoService.buscarEntidade(1L)).thenReturn(servico);
        when(horarioRepository.buscarComLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Horário não encontrado");
    }

    // ── buscarPorId ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: retorna agendamento existente")
    void buscarPorId_sucesso() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        AgendamentoResponse response = agendamentoService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId: lança EntityNotFoundException quando não encontrado")
    void buscarPorId_naoEncontrado() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── listarPorData ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorData: retorna lista de agendamentos")
    void listarPorData_sucesso() {
        when(agendamentoRepository.findByData(any())).thenReturn(List.of(agendamento));

        List<AgendamentoResponse> result = agendamentoService.listarPorData(LocalDate.now());

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarPorData: retorna lista vazia")
    void listarPorData_vazio() {
        when(agendamentoRepository.findByData(any())).thenReturn(List.of());

        List<AgendamentoResponse> result = agendamentoService.listarPorData(LocalDate.now());

        assertThat(result).isEmpty();
    }

    // ── listarPorCliente ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorCliente: retorna agendamentos do cliente")
    void listarPorCliente_sucesso() {
        when(agendamentoRepository.findByClienteId(1L)).thenReturn(List.of(agendamento));

        List<AgendamentoResponse> result = agendamentoService.listarPorCliente(1L);

        assertThat(result).hasSize(1);
    }

    // ── listarPorStatus ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorStatus: retorna agendamentos com status filtrado")
    void listarPorStatus_sucesso() {
        when(agendamentoRepository.findByStatusAgendamento(StatusAgendamento.AGENDADO))
                .thenReturn(List.of(agendamento));

        List<AgendamentoResponse> result = agendamentoService.listarPorStatus(StatusAgendamento.AGENDADO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatusAgendamento()).isEqualTo(StatusAgendamento.AGENDADO);
    }

    // ── confirmar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("confirmar: confirma agendamento com sucesso")
    void confirmar_sucesso() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        doNothing().when(agendamentoValidator).validarConfirmacao(any());
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.confirmar(1L);

        assertThat(response).isNotNull();
        verify(agendamentoRepository).save(any());
    }

    // ── remarcar ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("remarcar: remarca agendamento com sucesso")
    void remarcar_sucesso() {
        HorarioDisponivel novoHorario = new HorarioDisponivel();
        novoHorario.setId(2L);
        novoHorario.setData(LocalDate.now().plusDays(3));
        novoHorario.setHoraInicio(LocalTime.of(10, 0));
        novoHorario.setHoraFim(LocalTime.of(18, 0));
        novoHorario.setAdmin(admin);
        novoHorario.setAgendamentos(new ArrayList<>());

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        doNothing().when(agendamentoValidator).validarRemarcacao(any());
        when(horarioRepository.buscarComLock(2L)).thenReturn(Optional.of(novoHorario));
        doNothing().when(agendamentoValidator).validarHorarioDentroDoIntervalo(any(), any(), any());
        doNothing().when(agendamentoValidator).validarConflito(any(), any(), any());
        doNothing().when(agendamentoValidator).validarAntecedenciaMinima(any(), any());
        doNothing().when(agendamentoValidator).validarServicoAtivo(any());
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.remarcar(1L, 2L);

        assertThat(response).isNotNull();
        verify(agendamentoRepository).save(any());
    }

    @Test
    @DisplayName("remarcar: lança EntityNotFoundException quando novo horário não encontrado")
    void remarcar_novoHorarioNaoEncontrado() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        doNothing().when(agendamentoValidator).validarRemarcacao(any());
        when(horarioRepository.buscarComLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.remarcar(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Horário não encontrado");
    }

    // ── cancelar ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelar: cancela agendamento com sucesso")
    void cancelar_sucesso() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        doNothing().when(agendamentoValidator).validarCancelamento(any());
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.cancelar(1L);

        assertThat(response).isNotNull();
        assertThat(agendamento.getStatusAgendamento()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    // ── realizarAtendimento ──────────────────────────────────────────────────────

    @Test
    @DisplayName("realizarAtendimento: realiza agendamento com sucesso")
    void realizarAtendimento_sucesso() {
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        doNothing().when(agendamentoValidator).validarRealizacao(any());
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.realizarAtendimento(1L);

        assertThat(response).isNotNull();
        assertThat(agendamento.getStatusAgendamento()).isEqualTo(StatusAgendamento.REALIZADO);
    }
}
