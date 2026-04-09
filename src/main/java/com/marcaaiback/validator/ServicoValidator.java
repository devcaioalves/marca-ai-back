package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ServicoValidator {

    private final ServicoRepository servicoRepository;

    public void validarServicoParaCadastro(Servico servico) {
        if(servico.getNome() == null || servico.getNome().trim().isEmpty()){
            throw new OperacaoNaoPermitidaException("O nome do serviço é obrigatório.");
        }

        String nomeLimpo = servico.getNome().trim();
        servico.setNome(nomeLimpo);

        if(servico.getValor() == null || servico.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacaoNaoPermitidaException("O valor do serviço é obrigatório e não pode ser negativos.");
        }

        if(servico.getDuracao() == null || servico.getDuracao() <= 0) {
            throw new OperacaoNaoPermitidaException("A duração do serviço é obrigatória e não pode ter valores negativos.");
        }

        if(servicoRepository.existsByNomeIgnoreCase(nomeLimpo)) {
            throw new RecursoDuplicadoException("Já existe um serviço com esse nome.");
        }
    }

    public void validarServicoParaAtualizar(Servico servico, Long idServicoExistente) {
        if(servico.getNome() != null){
            String nomeLimpo = servico.getNome().trim();

            if(nomeLimpo.isEmpty()){
                throw new OperacaoNaoPermitidaException("O nome do serviço é obrigatório.");
            }

            boolean nomeDuplicado = servicoRepository.existsByNomeIgnoreCaseAndIdNot(nomeLimpo, idServicoExistente);
            if(nomeDuplicado) {
                throw new RecursoDuplicadoException("Já existe outro serviço com esse nome.");
            }
            servico.setNome(nomeLimpo);
        }

        if(servico.getValor() != null && servico.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacaoNaoPermitidaException("O valor do serviço é obrigatório e não pode ser negativos.");
        }

        if(servico.getDuracao() != null && servico.getDuracao() <= 0) {
            throw new OperacaoNaoPermitidaException("A duração do serviço é obrigatória e não pode ter valores negativos.");
        }
    }
}
