package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteValidator {

    private final ClienteRepository clienteRepository;

    public String validarEPadronizarTelefone(String telefone){
        if(telefone == null || telefone.trim().isEmpty()){
            throw new OperacaoNaoPermitidaException("O telefone não pode ser vazio.");
        }

        String telefoneFormatado = telefone.replaceAll("\\D", "");

        if(telefoneFormatado.length() != 11){
            throw new OperacaoNaoPermitidaException("O telefone precisa ser de 11 dígitos.");
        }
        return telefoneFormatado;

    }
}
