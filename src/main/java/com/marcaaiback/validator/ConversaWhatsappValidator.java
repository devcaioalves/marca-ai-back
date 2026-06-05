package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.enuns.EstadoConversa;
import org.springframework.stereotype.Component;

@Component
public class ConversaWhatsappValidator {

    public void validarEstado(EstadoConversa estadoConversa) {

        if (estadoConversa == null) {
            throw new OperacaoNaoPermitidaException("O estado da conversa é obrigatório.");
        }
    }

    public void validarClienteExiste(String telefone) {

        if (telefone == null || telefone.trim().isEmpty()) {
            throw new OperacaoNaoPermitidaException("O telefone do cliente é obrigatório.");
        }
    }
}