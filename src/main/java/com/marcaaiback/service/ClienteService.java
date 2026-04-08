package com.marcaaiback.service;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.exception.RecursoNaoEncontradoException;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.ClienteRepository;
import com.marcaaiback.validator.ClienteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteValidator clienteValidator;

    public Cliente buscarClientePorId(Long id){
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
    }

    public Cliente buscarClientePorTelefone(String telefone){
        String telefoneFormatado = clienteValidator.validarEPadronizarTelefone(telefone);
        return clienteRepository.findByTelefone(telefoneFormatado);
    }

    public List<Cliente> listarClientePorNome(String nome){
        if(nome == null || nome.trim().isEmpty()){
            throw new OperacaoNaoPermitidaException("O nome é obrigatório.");
        }
        String nomeLimpo = nome.trim();

        Cliente cliente = new Cliente();
        cliente.setNome(nomeLimpo);

        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Cliente> example = Example.of(cliente, matcher);

        return clienteRepository.findAll(example);
    }

    public List<Cliente> listarClientes(){
        return clienteRepository.findAll();
    }

    public Cliente salvarCliente(Cliente cliente){
        String telefoneFormatado = clienteValidator.validarEPadronizarTelefone(cliente.getTelefone());
        cliente.setTelefone(telefoneFormatado);
        Cliente clienteExistente = buscarClientePorTelefone(telefoneFormatado);
        if(clienteExistente != null){
            return clienteExistente;
        }
        return clienteRepository.save(cliente);
    }
}
