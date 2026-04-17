package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.dto.servico.ServicoRequest;
import com.marcaaiback.model.dto.servico.ServicoResponse;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.ServicoRepository;
import com.marcaaiback.validator.ServicoValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoValidator servicoValidator;

    public ServicoResponse criar(ServicoRequest request) {

        // validações centralizadas
        servicoValidator.validarNome(request.getNome());
        servicoValidator.validarValor(request.getValor());
        servicoValidator.validarDuracao(request.getDuracao());
        servicoValidator.validarDuplicidade(request.getNome());

        Servico servico = new Servico();
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setValor(request.getValor());
        servico.setDuracao(request.getDuracao());
        servico.setAtivo(true);

        return toResponse(servicoRepository.save(servico));
    }

    public ServicoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public List<ServicoResponse> listarTodos() {
        return servicoRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ServicoResponse> listarAtivos() {
        List<Servico> servicos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
        if (servicos.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Não há nenhum serviço ativo.");
        }

        return servicos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ServicoResponse atualizar(Long id, ServicoRequest request) {

        Servico servico = buscarEntidade(id);

        // validações centralizadas
        servicoValidator.validarNome(request.getNome());
        servicoValidator.validarValor(request.getValor());
        servicoValidator.validarDuracao(request.getDuracao());
        servicoValidator.validarDuplicidadeNaAtualizacao(request.getNome(), id);

        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setValor(request.getValor());
        servico.setDuracao(request.getDuracao());

        return toResponse(servicoRepository.save(servico));
    }

    public void ativarDesativar(Long id) {
        Servico servico = buscarEntidade(id);
        servico.setAtivo(!servico.isAtivo());
        servicoRepository.save(servico);
    }

    public void deletar(Long id) {
        Servico servico = buscarEntidade(id);

        if (servico.getAgendamentos() != null && !servico.getAgendamentos().isEmpty()) {
            throw new OperacaoNaoPermitidaException("Não é possível excluir um serviço com agendamentos vinculados.");
        }

        servicoRepository.delete(servico);
    }

    // método interno reutilizável pelos outros services
    public Servico buscarEntidade(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado."));
    }

    private ServicoResponse toResponse(Servico servico) {
        ServicoResponse response = new ServicoResponse();
        response.setId(servico.getId());
        response.setNome(servico.getNome());
        response.setDescricao(servico.getDescricao());
        response.setValor(servico.getValor());
        response.setDuracao(servico.getDuracao());
        response.setAtivo(servico.isAtivo());
        response.setAgendamentos(List.of()); // carregado separadamente se necessário
        return response;
    }
}