package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Notificacao;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.NotificacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoValidatorTest {

    @Mock
    NotificacaoRepository notificacaoRepository;

    @InjectMocks
    NotificacaoValidator validator;

    @Test
    @DisplayName("validarTipoMensagem: não lança com tipo válido")
    void validarTipo_valido() {
        assertThatCode(() -> validator.validarTipoMensagem(TipoDeMensagem.CONFIRMACAO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarTipoMensagem: lança quando nulo")
    void validarTipo_nulo() {
        assertThatThrownBy(() -> validator.validarTipoMensagem(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarAgendamentoParaNotificacao: lança quando cancelado")
    void validarAgendamento_cancelado() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.CANCELADO);

        assertThatThrownBy(() -> validator.validarAgendamentoParaNotificacao(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("cancelado");
    }

    @Test
    @DisplayName("validarAgendamentoParaNotificacao: não lança quando confirmado")
    void validarAgendamento_confirmado() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        assertThatCode(() -> validator.validarAgendamentoParaNotificacao(ag))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarDuplicidade: lança quando notificação já enviada")
    void validarDuplicidade_existe() {
        when(notificacaoRepository.existsByAgendamentoIdAndTipoDeMensagem(1L, TipoDeMensagem.CONFIRMACAO))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validarDuplicidade(1L, TipoDeMensagem.CONFIRMACAO))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarDuplicidade: não lança quando não existe")
    void validarDuplicidade_naoExiste() {
        when(notificacaoRepository.existsByAgendamentoIdAndTipoDeMensagem(1L, TipoDeMensagem.TEXTO))
                .thenReturn(false);

        assertThatCode(() -> validator.validarDuplicidade(1L, TipoDeMensagem.TEXTO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarMarcacaoComoLida: lança quando já lida")
    void validarMarcacao_jaLida() {
        Notificacao n = new Notificacao();
        n.setStatusNotificacao(StatusNotificacao.LIDO);

        assertThatThrownBy(() -> validator.validarMarcacaoComoLida(n))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarMarcacaoComoLida: não lança quando enviada")
    void validarMarcacao_enviada() {
        Notificacao n = new Notificacao();
        n.setStatusNotificacao(StatusNotificacao.ENVIADO);

        assertThatCode(() -> validator.validarMarcacaoComoLida(n)).doesNotThrowAnyException();
    }
}
