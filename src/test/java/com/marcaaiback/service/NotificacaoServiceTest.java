package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.notificacao.NotificacaoRequest;
import com.marcaaiback.model.dto.notificacao.NotificacaoResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.NotificacaoRepository;
import com.marcaaiback.validator.NotificacaoValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock NotificacaoRepository notificacaoRepository;
    @Mock AgendamentoService agendamentoService;
    @Mock NotificacaoValidator notificacaoValidator;

    @InjectMocks NotificacaoService notificacaoService;

    private Cliente cliente;
    private Agendamento agendamento;
    private Notificacao notificacao;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        cliente.setTelefone("5581999999999");

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(cliente);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);

        notificacao = new Notificacao();
        notificacao.setId(1L);
        notificacao.setTipoDeMensagem(TipoDeMensagem.CONFIRMACAO);
        notificacao.setDataEnvio(LocalDateTime.now());
        notificacao.setStatusNotificacao(StatusNotificacao.ENVIADO);
        notificacao.setAgendamento(agendamento);
    }

    @Test
    @DisplayName("disparar: dispara notificação com sucesso")
    void disparar_sucesso() {
        NotificacaoRequest request = new NotificacaoRequest(TipoDeMensagem.CONFIRMACAO, 1L);

        when(agendamentoService.buscarEntidade(1L)).thenReturn(agendamento);
        doNothing().when(notificacaoValidator).validarTipoMensagem(any());
        doNothing().when(notificacaoValidator).validarAgendamentoParaNotificacao(any());
        doNothing().when(notificacaoValidator).validarDuplicidade(anyLong(), any());
        when(notificacaoRepository.save(any())).thenReturn(notificacao);

        NotificacaoResponse response = notificacaoService.disparar(request);

        assertThat(response).isNotNull();
        assertThat(response.getTipoDeMensagem()).isEqualTo(TipoDeMensagem.CONFIRMACAO);
        assertThat(response.getClienteNome()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("listarPorAgendamento: retorna notificações existentes")
    void listarPorAgendamento_sucesso() {
        when(notificacaoRepository.findByAgendamentoId(1L)).thenReturn(List.of(notificacao));

        List<NotificacaoResponse> result = notificacaoService.listarPorAgendamento(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarPorAgendamento: lança EntidadeNaoEncontradaException quando vazio")
    void listarPorAgendamento_vazio() {
        when(notificacaoRepository.findByAgendamentoId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> notificacaoService.listarPorAgendamento(1L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("listarPorStatus: retorna notificações por status")
    void listarPorStatus_sucesso() {
        when(notificacaoRepository.findByStatusNotificacao(StatusNotificacao.ENVIADO))
                .thenReturn(List.of(notificacao));

        List<NotificacaoResponse> result = notificacaoService.listarPorStatus(StatusNotificacao.ENVIADO);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarPorStatus: lança EntidadeNaoEncontradaException quando vazio")
    void listarPorStatus_vazio() {
        when(notificacaoRepository.findByStatusNotificacao(StatusNotificacao.LIDO)).thenReturn(List.of());

        assertThatThrownBy(() -> notificacaoService.listarPorStatus(StatusNotificacao.LIDO))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("marcarComoLida: marca notificação como lida com sucesso")
    void marcarComoLida_sucesso() {
        when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao));
        doNothing().when(notificacaoValidator).validarMarcacaoComoLida(any());
        when(notificacaoRepository.save(any())).thenReturn(notificacao);

        assertThatCode(() -> notificacaoService.marcarComoLida(1L)).doesNotThrowAnyException();
        assertThat(notificacao.getStatusNotificacao()).isEqualTo(StatusNotificacao.LIDO);
    }

    @Test
    @DisplayName("marcarComoLida: lança EntityNotFoundException quando não encontrada")
    void marcarComoLida_naoEncontrada() {
        when(notificacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificacaoService.marcarComoLida(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
