package com.marcaaiback.service;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoNaoEncontradoException;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.AgendamentoRepository;
import com.marcaaiback.repository.ServicoRepository;
import com.marcaaiback.validator.ServicoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ServicoValidator servicoValidator;

    public Servico buscarServicoPorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));
    }

    public List<Servico> listarServicoPorNome(String nome){
        if(nome == null || nome.trim().isEmpty()){
            throw new OperacaoNaoPermitidaException("O nome é obrigatório.");
        }
        String nomeLimpo = nome.trim();

        Servico servico = new Servico();
        servico.setNome(nomeLimpo);

        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Servico> example = Example.of(servico, matcher);

        return servicoRepository.findAll(example);
    }

    public List<Servico> listarTodosServicos(){
        return servicoRepository.findAllByOrderByNomeAsc();
    }

    public List<Servico> listarServicosAtivos(){
        return servicoRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public Servico cadastrarServico(Servico servico) {
        servicoValidator.validarServicoParaCadastro(servico);
        return servicoRepository.save(servico);
    }

    public Servico atualizarServico(Servico servico, Long id) {
        Servico servicoExistente = buscarServicoPorId(id);
        servicoValidator.validarServicoParaAtualizar(servico, servicoExistente.getId());

        if(servico.getNome() != null){
            servicoExistente.setNome(servico.getNome());
        }

        if(servico.getDescricao() != null){
            servicoExistente.setDescricao(servico.getDescricao());
        }

        if(servico.getValor() != null){
           servicoExistente.setValor(servico.getValor());
        }

        if(servico.getDuracao() != null){
           servicoExistente.setDuracao(servico.getDuracao());
        }
        return servicoRepository.save(servicoExistente);
    }

    public Servico removerServico(Long id) {
        Servico servicoExistente = buscarServicoPorId(id);

        if(!servicoExistente.isAtivo()){
            throw new OperacaoNaoPermitidaException("Serviço já está inativo.");
        }

        boolean existeAgendamento = agendamentoRepository.existsByServico(servicoExistente);
        if(existeAgendamento){
            throw new OperacaoNaoPermitidaException("Não é possível inativar serviço com ageendamentos.");
        }
        servicoExistente.setAtivo(false);
        return servicoRepository.save(servicoExistente);
    }
}
