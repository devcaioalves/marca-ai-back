package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Notificacao;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificacaoValidator {

    private final NotificacaoRepository notificacaoRepository;

    public void validarTipoMensagem(TipoDeMensagem tipo) {
        if (tipo == null) {
            throw new OperacaoNaoPermitidaException("O tipo da mensagem é obrigatório.");
        }
    }

    public void validarAgendamentoParaNotificacao(Agendamento agendamento) {
        if (agendamento.getStatusAgendamento() == StatusAgendamento.CANCELADO) {
            throw new OperacaoNaoPermitidaException(
                    "Não é possível enviar notificação para um agendamento cancelado."
            );
        }
    }

    public void validarDuplicidade(Long agendamentoId, TipoDeMensagem tipoMensagem) {
        boolean existe = notificacaoRepository
                .existsByAgendamentoIdAndTipoDeMensagem(agendamentoId, tipoMensagem);

        if (existe) {
            throw new OperacaoNaoPermitidaException(
                    "Já foi enviada uma notificação desse tipo para este agendamento."
            );
        }
    }

    public void validarMarcacaoComoLida(Notificacao notificacao) {
        if (notificacao.getStatusNotificacao() == StatusNotificacao.LIDO) {
            throw new OperacaoNaoPermitidaException(
                    "A notificação já está marcada como lida."
            );
        }
    }
}