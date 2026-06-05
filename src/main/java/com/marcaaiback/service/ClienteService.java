package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.agendamento.AgendamentoResumoResponse;
import com.marcaaiback.model.dto.cliente.ClienteRequest;
import com.marcaaiback.model.dto.cliente.ClienteResponse;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.repository.ClienteRepository;
import com.marcaaiback.validator.ClienteValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteValidator clienteValidator;

    public ClienteResponse criar(ClienteRequest request) {
        // valida e padroniza telefone
        String telefonePadronizado = clienteValidator.validarEPadronizarTelefone(request.getTelefone());
        // verifica duplicidade
        clienteValidator.validarTelefoneDuplicado(telefonePadronizado);

        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setTelefone(telefonePadronizado);

        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public List<ClienteResponse> buscarPorNomeOuTelefone(String termo) {
        List<Cliente> clientes = clienteRepository
                .findByNomeContainingIgnoreCaseOrTelefoneContaining(
                        termo,
                        termo
                );
        if(clientes.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum cliente encontrado.");
        }

        return clientes.stream()
                .map(this:: toResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponse buscarPorTelefone(String telefone) {
        String telefonePadronizado = clienteValidator.validarEPadronizarTelefone(telefone);
        System.out.println("Telefone padronizado: " + telefonePadronizado);
        Cliente cliente = clienteRepository.findByTelefone(telefonePadronizado)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));
        return toResponse(cliente);
    }

    public List<ClienteResponse> listarTodos() {

        List<Cliente> clientes = clienteRepository.findAll();

        if (clientes.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhum cliente encontrado.");
        }

        return clientes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);

        String telefonePadronizado = clienteValidator.validarEPadronizarTelefone(request.getTelefone());
        clienteValidator.validarTelefoneDuplicadoNaAtualizacao(telefonePadronizado, cliente.getTelefone());

        cliente.setNome(request.getNome());
        cliente.setTelefone(telefonePadronizado);

        return toResponse(clienteRepository.save(cliente));
    }

    public void deletar(Long id) {
        Cliente cliente = buscarEntidade(id);
        clienteRepository.delete(cliente);
    }

    public Cliente buscarClientePorTelefoneEntidade(String telefone) {

        return clienteRepository.findByTelefone(telefone)
                .orElseThrow(() ->
                        new EntidadeNaoEncontradaException("Cliente não encontrado."));
    }

    // método interno reutilizável pelos outros services
    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));
    }

    private ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNome(cliente.getNome());
        response.setTelefone(cliente.getTelefone());

        List<AgendamentoResumoResponse> agendamentos = cliente.getAgendamentos() == null
                ? List.of()
                : cliente.getAgendamentos().stream().map(a -> {
            AgendamentoResumoResponse resumo = new AgendamentoResumoResponse();
            resumo.setId(a.getId());
            resumo.setData(a.getData());
            resumo.setHoraInicio(a.getHoraInicio());
            resumo.setHoraFim(a.getHoraFim());
            resumo.setStatusAgendamento(a.getStatusAgendamento());
            resumo.setServicoNome(a.getServico().getNome());
            resumo.setClienteNome(a.getCliente().getNome());
            return resumo;
        }).collect(Collectors.toList());

        response.setAgendamentos(agendamentos);
        return response;
    }
}
