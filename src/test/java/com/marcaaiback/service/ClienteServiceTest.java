package com.marcaaiback.service;

import com.marcaaiback.model.dto.cliente.ClienteRequest;
import com.marcaaiback.model.dto.cliente.ClienteResponse;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.repository.ClienteRepository;
import com.marcaaiback.validator.ClienteValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteValidator clienteValidator;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente criarCliente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNome("João Silva");
        c.setTelefone("11999998888");
        c.setAgendamentos(List.of());
        return c;
    }

    private ClienteRequest criarRequest() {
        ClienteRequest r = new ClienteRequest();
        r.setNome("João Silva");
        r.setTelefone("(11) 99999-8888");
        return r;
    }

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteValidator.validarEPadronizarTelefone("(11) 99999-8888")).thenReturn("11999998888");
        when(clienteRepository.save(any())).thenReturn(criarCliente());

        ClienteResponse response = clienteService.criar(criarRequest());

        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getTelefone()).isEqualTo("11999998888");
        verify(clienteValidator).validarTelefoneDuplicado("11999998888");
    }

    @Test
    void deveBuscarClientePorId() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(criarCliente()));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontradoPorId() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void deveBuscarClientePorTelefone() {
        when(clienteValidator.validarEPadronizarTelefone("11999998888")).thenReturn("11999998888");
        when(clienteRepository.findByTelefone("11999998888")).thenReturn(Optional.of(criarCliente()));

        ClienteResponse response = clienteService.buscarPorTelefone("11999998888");

        assertThat(response.getTelefone()).isEqualTo("11999998888");
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontradoPorTelefone() {
        when(clienteValidator.validarEPadronizarTelefone("11999998888")).thenReturn("11999998888");
        when(clienteRepository.findByTelefone("11999998888")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorTelefone("11999998888"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveListarTodosClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(criarCliente()));

        List<ClienteResponse> lista = clienteService.listarTodos();

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveAtualizarCliente() {
        Cliente cliente = criarCliente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteValidator.validarEPadronizarTelefone("(11) 99999-8888")).thenReturn("11999998888");
        when(clienteRepository.save(any())).thenReturn(cliente);

        ClienteResponse response = clienteService.atualizar(1L, criarRequest());

        assertThat(response.getNome()).isEqualTo("João Silva");
        verify(clienteValidator).validarTelefoneDuplicadoNaAtualizacao("11999998888", "11999998888");
    }

    @Test
    void deveDeletarCliente() {
        Cliente cliente = criarCliente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.deletar(1L);

        verify(clienteRepository).delete(cliente);
    }
}
