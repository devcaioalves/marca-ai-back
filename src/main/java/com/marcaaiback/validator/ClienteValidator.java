package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteValidator {

    private final ClienteRepository clienteRepository;

    public String validarEPadronizarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new OperacaoNaoPermitidaException("O telefone não pode ser vazio.");
        }

        String telefoneFormatado = telefone.replaceAll("\\D", "");

        if (telefoneFormatado.length() != 11) {
            throw new OperacaoNaoPermitidaException("O telefone deve conter 11 dígitos (DDD + número).");
        }

        String ddd = telefoneFormatado.substring(0, 2);
        int dddNumero = Integer.parseInt(ddd);
        if (dddNumero < 11 || dddNumero > 99) {
            throw new OperacaoNaoPermitidaException("DDD inválido: " + ddd);
        }

        // celular brasileiro sempre começa com 9 após o DDD
        if (telefoneFormatado.charAt(2) != '9') {
            throw new OperacaoNaoPermitidaException("Número de celular inválido. Deve começar com 9 após o DDD.");
        }

        return "55" + telefoneFormatado;
    }

    public void validarTelefoneDuplicado(String telefone) {
        if (clienteRepository.existsByTelefone(telefone)) {
            throw new RecursoDuplicadoException("Já existe um cliente cadastrado com esse telefone.");
        }
    }

    public void validarTelefoneDuplicadoNaAtualizacao(String telefoneNovo, String telefoneAtual) {
        if (!telefoneAtual.equals(telefoneNovo) && clienteRepository.existsByTelefone(telefoneNovo)) {
            throw new RecursoDuplicadoException("Já existe um cliente cadastrado com esse telefone.");
        }
    }
}