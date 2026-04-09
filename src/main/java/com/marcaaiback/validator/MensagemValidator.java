package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MensagemValidator {

    public void validarConteudo(String conteudo) {
        if (conteudo == null || conteudo.trim().isEmpty()) {
            throw new OperacaoNaoPermitidaException("A mensagem não pode ser vazia.");
        }

        if (conteudo.length() > 500) {
            throw new OperacaoNaoPermitidaException("A mensagem deve ter no máximo 500 caracteres.");
        }
    }

    public void validarClienteDoAgendamento(Cliente cliente, Agendamento agendamento) {
        if (!agendamento.getCliente().getId().equals(cliente.getId())) {
            throw new OperacaoNaoPermitidaException(
                    "O cliente não pertence a esse agendamento."
            );
        }
    }

    public void validarTipo(TipoDeMensagem tipo) {
        if (tipo == null) {
            throw new OperacaoNaoPermitidaException("O tipo da mensagem é obrigatório.");
        }
    }
}