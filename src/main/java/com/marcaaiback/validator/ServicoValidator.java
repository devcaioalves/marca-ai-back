package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ServicoValidator {

    private final ServicoRepository servicoRepository;

    public void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new OperacaoNaoPermitidaException("O nome do serviço é obrigatório.");
        }
    }

    public void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacaoNaoPermitidaException(
                    "O valor do serviço deve ser maior que zero."
            );
        }
    }

    public void validarDuracao(Integer duracao) {
        if (duracao == null || duracao <= 0) {
            throw new OperacaoNaoPermitidaException("A duração deve ser maior que zero.");
        }
    }

    public void validarDuplicidade(String nome) {
        if (servicoRepository.existsByNomeIgnoreCase(nome)) {
            throw new RecursoDuplicadoException("Já existe um serviço com esse nome.");
        }
    }

    public void validarDuplicidadeNaAtualizacao(String nome, Long id) {
        if (servicoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new RecursoDuplicadoException("Já existe um serviço com esse nome.");
        }
    }
}