package com.marcaaiback.service;

import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.validator.AgendamentoValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock private AgendamentoRepository agendamentoRepository;
    @Mock private ClienteService clienteService;
    @Mock private ServicoService servicoService;
    @Mock private HorarioDisponivelService horarioDisponivelService;
    @Mock private AdminService adminService;
    @Mock private AgendamentoValidator agendamentoValidator;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Agendamento criarAgendamento() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");

        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte");
        servico.setDuracao(30);

        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setId(1L);
        horario.setData(LocalDate.now());
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFim(LocalTime.of(10, 0));
        horario.setDisponivel(true);

        Agendamento a = new Agendamento();
        a.setId(1L);
        a.setCliente(cliente);
        a.setServico(servico);
        a.setHorarioDisponivel(horario);
        a.setAdmin(new Admin());
        a.setData(LocalDate.now());
        a.setHoraInicio(LocalTime.of(9, 0));
        a.setHoraFim(LocalTime.of(9, 30));
        a.setStatusAgendamento(StatusAgendamento.CONFIRMADO);
        return a;
    }

//    @Test
//    void deveCriarAgendamentoComSucesso() {
//        Agendamento agendamento = criarAgendamento();
//
//        AgendamentoRequest request = new AgendamentoRequest();
//        request.setClienteId(1L);
//        request.setServicoId(1L);
//        request.setHorarioDisponivelId(1L);
//        request.setHoraInicio(LocalTime.of(10, 0));
//        request.setHoraFim(LocalTime.of(11, 0));
//
//        when(clienteService.buscarEntidade(1L)).thenReturn(agendamento.getCliente());
//        when(servicoService.buscarEntidade(1L)).thenReturn(agendamento.getServico());
//        when(horarioDisponivelService.buscarEntidade(1L)).thenReturn(agendamento.getHorarioDisponivel());
//        when(adminService.buscarEntidade()).thenReturn(new Admin());
//        when(agendamentoRepository.save(any())).thenReturn(agendamento);
//
//        AgendamentoResponse response = agendamentoService.criar(request);
//
//        assertThat(response.getStatusAgendamento())
//                .isEqualTo(StatusAgendamento.CONFIRMADO);
//
//        verify(agendamentoValidator).validarHorarioDentroDoIntervalo(
//                eq(agendamento.getHorarioDisponivel()),
//                any(LocalTime.class),
//                any(LocalTime.class)
//        );
//
//        verify(horarioDisponivelService).salvar(any());
//    }

    @Test
    void deveBuscarAgendamentoPorId() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(criarAgendamento()));

        AgendamentoResponse response = agendamentoService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void deveLancarExcecaoQuandoAgendamentoNaoEncontrado() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveListarPorData() {
        when(agendamentoRepository.findByData(any())).thenReturn(List.of(criarAgendamento()));

        List<AgendamentoResponse> lista = agendamentoService.listarPorData(LocalDate.now());

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveListarPorCliente() {
        when(agendamentoRepository.findByClienteId(1L)).thenReturn(List.of(criarAgendamento()));

        List<AgendamentoResponse> lista = agendamentoService.listarPorCliente(1L);

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveListarPorStatus() {
        when(agendamentoRepository.findByStatusAgendamento(StatusAgendamento.CONFIRMADO))
                .thenReturn(List.of(criarAgendamento()));

        List<AgendamentoResponse> lista = agendamentoService.listarPorStatus(StatusAgendamento.CONFIRMADO);

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveCancelarAgendamento() {
        Agendamento agendamento = criarAgendamento();
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.cancelar(1L);

        assertThat(response.getStatusAgendamento()).isEqualTo(StatusAgendamento.CANCELADO);
        verify(agendamentoValidator).validarCancelamento(agendamento);
        verify(horarioDisponivelService).salvar(any());
    }

    @Test
    void deveRealizarAtendimento() {
        Agendamento agendamento = criarAgendamento();
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        AgendamentoResponse response = agendamentoService.realizarAtendimento(1L);

        assertThat(response.getStatusAgendamento()).isEqualTo(StatusAgendamento.REALIZADO);
        verify(agendamentoValidator).validarRealizacao(agendamento);
    }
}
