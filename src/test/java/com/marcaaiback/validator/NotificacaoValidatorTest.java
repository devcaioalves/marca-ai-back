package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Notificacao;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.NotificacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoValidatorTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private NotificacaoValidator notificacaoValidator;

    // ---- validarTipoMensagem ----

    @Test
    void naoDeveLancarExcecaoQuandoTipoValido() {
        assertThatCode(() -> notificacaoValidator.validarTipoMensagem(TipoDeMensagem.CONFIRMACAO))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoTipoNulo() {
        assertThatThrownBy(() -> notificacaoValidator.validarTipoMensagem(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("obrigatório");
    }

    // ---- validarAgendamentoParaNotificacao ----

    @Test
    void deveLancarExcecaoQuandoAgendamentoCancelado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);

        assertThatThrownBy(() -> notificacaoValidator.validarAgendamentoParaNotificacao(agendamento))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("cancelado");
    }

    @Test
    void naoDeveLancarExcecaoQuandoAgendamentoConfirmado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        assertThatCode(() -> notificacaoValidator.validarAgendamentoParaNotificacao(agendamento))
                .doesNotThrowAnyException();
    }

    // ---- validarDuplicidade ----

    @Test
    void deveLancarExcecaoQuandoNotificacaoDuplicada() {
        when(notificacaoRepository.existsByAgendamentoIdAndTipoDeMensagem(1L, TipoDeMensagem.LEMBRETE))
                .thenReturn(true);

        assertThatThrownBy(() -> notificacaoValidator.validarDuplicidade(1L, TipoDeMensagem.LEMBRETE))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já foi enviada");
    }

    @Test
    void naoDeveLancarExcecaoQuandoNotificacaoNaoDuplicada() {
        when(notificacaoRepository.existsByAgendamentoIdAndTipoDeMensagem(1L, TipoDeMensagem.LEMBRETE))
                .thenReturn(false);

        assertThatCode(() -> notificacaoValidator.validarDuplicidade(1L, TipoDeMensagem.LEMBRETE))
                .doesNotThrowAnyException();
    }

    // ---- validarMarcacaoComoLida ----

    @Test
    void deveLancarExcecaoQuandoNotificacaoJaLida() {
        Notificacao notificacao = new Notificacao();
        notificacao.setStatusNotificacao(StatusNotificacao.LIDO);

        assertThatThrownBy(() -> notificacaoValidator.validarMarcacaoComoLida(notificacao))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("já está marcada como lida");
    }

    @Test
    void naoDeveLancarExcecaoQuandoNotificacaoNaoLida() {
        Notificacao notificacao = new Notificacao();
        notificacao.setStatusNotificacao(StatusNotificacao.ENVIADO);

        assertThatCode(() -> notificacaoValidator.validarMarcacaoComoLida(notificacao))
                .doesNotThrowAnyException();
    }
}
