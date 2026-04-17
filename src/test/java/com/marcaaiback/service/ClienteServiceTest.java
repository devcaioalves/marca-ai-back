package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.cliente.ClienteRequest;
import com.marcaaiback.model.dto.cliente.ClienteResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.ClienteRepository;
import com.marcaaiback.validator.ClienteValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @Mock ClienteValidator clienteValidator;

    @InjectMocks ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        cliente.setTelefone("5581999999999");
        cliente.setAgendamentos(new ArrayList<>());
    }

    // ── criar ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criar: sucesso com dados válidos")
    void criar_sucesso() {
        ClienteRequest request = new ClienteRequest("Maria", "81999999999");

        when(clienteValidator.validarEPadronizarTelefone("81999999999")).thenReturn("5581999999999");
        doNothing().when(clienteValidator).validarTelefoneDuplicado("5581999999999");
        when(clienteRepository.save(any())).thenReturn(cliente);

        ClienteResponse response = clienteService.criar(request);

        assertThat(response.getNome()).isEqualTo("Maria");
        assertThat(response.getTelefone()).isEqualTo("5581999999999");
    }

    // ── buscarPorId ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: retorna cliente existente")
    void buscarPorId_sucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId: lança EntityNotFoundException quando não encontrado")
    void buscarPorId_naoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    // ── buscarPorTelefone ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorTelefone: retorna cliente com telefone válido")
    void buscarPorTelefone_sucesso() {
        when(clienteValidator.validarEPadronizarTelefone("81999999999")).thenReturn("5581999999999");
        when(clienteRepository.findByTelefone("5581999999999")).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscarPorTelefone("81999999999");

        assertThat(response.getTelefone()).isEqualTo("5581999999999");
    }

    @Test
    @DisplayName("buscarPorTelefone: lança EntityNotFoundException quando não encontrado")
    void buscarPorTelefone_naoEncontrado() {
        when(clienteValidator.validarEPadronizarTelefone("81900000000")).thenReturn("5581900000000");
        when(clienteRepository.findByTelefone("5581900000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorTelefone("81900000000"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── listarTodos ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodos: retorna lista de clientes")
    void listarTodos_sucesso() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<ClienteResponse> result = clienteService.listarTodos();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarTodos: lança EntidadeNaoEncontradaException quando lista vazia")
    void listarTodos_vazio() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> clienteService.listarTodos())
                .isInstanceOf(EntidadeNaoEncontradaException.class)
                .hasMessageContaining("Nenhum cliente encontrado");
    }

    // ── atualizar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("atualizar: atualiza dados do cliente com sucesso")
    void atualizar_sucesso() {
        ClienteRequest request = new ClienteRequest("Maria Atualizada", "81988888888");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteValidator.validarEPadronizarTelefone("81988888888")).thenReturn("5581988888888");
        doNothing().when(clienteValidator).validarTelefoneDuplicadoNaAtualizacao("5581988888888", "5581999999999");
        when(clienteRepository.save(any())).thenReturn(cliente);

        ClienteResponse response = clienteService.atualizar(1L, request);

        assertThat(response).isNotNull();
        verify(clienteRepository).save(any());
    }

    @Test
    @DisplayName("atualizar: lança EntityNotFoundException quando cliente não existe")
    void atualizar_naoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.atualizar(99L, new ClienteRequest("x", "81999999999")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── deletar ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletar: remove cliente com sucesso")
    void deletar_sucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        assertThatCode(() -> clienteService.deletar(1L)).doesNotThrowAnyException();
        verify(clienteRepository).delete(cliente);
    }

    @Test
    @DisplayName("deletar: lança EntityNotFoundException quando cliente não existe")
    void deletar_naoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.deletar(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── toResponse com agendamentos ──────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: mapeia agendamentos do cliente corretamente")
    void buscarPorId_comAgendamentos() {
        Servico servico = new Servico();
        servico.setNome("Corte");

        Agendamento ag = new Agendamento();
        ag.setId(10L);
        ag.setData(LocalDate.now());
        ag.setHoraInicio(LocalTime.of(9, 0));
        ag.setHoraFim(LocalTime.of(9, 30));
        ag.setStatusAgendamento(StatusAgendamento.AGENDADO);
        ag.setServico(servico);
        ag.setCliente(cliente);

        cliente.setAgendamentos(List.of(ag));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertThat(response.getAgendamentos()).hasSize(1);
        assertThat(response.getAgendamentos().get(0).getServicoNome()).isEqualTo("Corte");
    }
}
