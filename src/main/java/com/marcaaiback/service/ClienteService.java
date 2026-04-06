package com.marcaaiback.service;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.exception.RecursoNaoEncontradoException;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public Cliente buscarClientePorId(Long id){
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
    }

    public Cliente salvarCliente(Cliente cliente){
        if(cliente.getNome() == null || cliente.getNome().isEmpty()){
            throw new OperacaoNaoPermitidaException("O nome é obrigatório e não pode ser vazio.");
        }

        if(cliente.getTelefone() == null || cliente.getTelefone().isEmpty()){
            throw new OperacaoNaoPermitidaException("O Telefone é obrigatório e não pode ser vazio.");
        }

        if(clienteRepository.existsByTelefone(cliente.getTelefone())){
            throw new RecursoDuplicadoException("Já existe um cliente com esse telefone.");
        }
        return clienteRepository.save(cliente);
    }

    public void excluirClientePorId(Long id){
        Cliente cliente = buscarClientePorId(id);
        clienteRepository.delete(cliente);
    }
}
