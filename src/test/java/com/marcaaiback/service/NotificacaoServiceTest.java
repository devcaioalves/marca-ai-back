package com.marcaaiback.service;

import com.marcaaiback.model.dto.notificacao.NotificacaoRequest;
import com.marcaaiback.model.dto.notificacao.NotificacaoResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Notificacao;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.NotificacaoRepository;
import com.marcaaiback.validator.NotificacaoValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private AgendamentoService agendamentoService;
    @Mock private NotificacaoValidator notificacaoValidator;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private Cliente criarCliente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNome("João");
        c.setTelefone("11999998888");
        return c;
    }

    private Agendamento criarAgendamento() {
        Agendamento a = new Agendamento();
        a.setId(1L);
        a.setCliente(criarCliente());
        a.setStatusAgendamento(StatusAgendamento.CONFIRMADO);
        return a;
    }

    private Notificacao criarNotificacao() {
        Notificacao n = new Notificacao();
        n.setId(1L);
        n.setTipoDeMensagem(TipoDeMensagem.CONFIRMACAO);
        n.setDataEnvio(LocalDateTime.now());
        n.setStatusNotificacao(StatusNotificacao.ENVIADO);
        n.setAgendamento(criarAgendamento());
        return n;
    }

    @Test
    void deveDispararNotificacaoComSucesso() {
        NotificacaoRequest request = new NotificacaoRequest(TipoDeMensagem.CONFIRMACAO, 1L);

        when(agendamentoService.buscarEntidade(1L)).thenReturn(criarAgendamento());
        when(notificacaoRepository.save(any())).thenReturn(criarNotificacao());

        NotificacaoResponse response = notificacaoService.disparar(request);

        assertThat(response.getTipoDeMensagem()).isEqualTo(TipoDeMensagem.CONFIRMACAO);
        assertThat(response.getStatusNotificacao()).isEqualTo(StatusNotificacao.ENVIADO);
        verify(notificacaoValidator).validarTipoMensagem(TipoDeMensagem.CONFIRMACAO);
        verify(notificacaoValidator).validarAgendamentoParaNotificacao(any());
        verify(notificacaoValidator).validarDuplicidade(anyLong(), any());
    }

    @Test
    void deveListarNotificacoesPorAgendamento() {
        when(notificacaoRepository.findByAgendamentoId(1L)).thenReturn(List.of(criarNotificacao()));

        List<NotificacaoResponse> lista = notificacaoService.listarPorAgendamento(1L);

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveListarNotificacoesPorStatus() {
        when(notificacaoRepository.findByStatusNotificacao(StatusNotificacao.ENVIADO))
                .thenReturn(List.of(criarNotificacao()));

        List<NotificacaoResponse> lista = notificacaoService.listarPorStatus(StatusNotificacao.ENVIADO);

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveMarcarNotificacaoComoLida() {
        Notificacao notificacao = criarNotificacao();
        when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao));

        notificacaoService.marcarComoLida(1L);

        assertThat(notificacao.getStatusNotificacao()).isEqualTo(StatusNotificacao.LIDO);
        verify(notificacaoValidator).validarMarcacaoComoLida(notificacao);
        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    void deveLancarExcecaoQuandoNotificacaoNaoEncontrada() {
        when(notificacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificacaoService.marcarComoLida(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
