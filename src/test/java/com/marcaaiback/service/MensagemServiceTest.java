package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.mensagem.MensagemRequest;
import com.marcaaiback.model.dto.mensagem.MensagemResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Mensagem;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.MensagemRepository;
import com.marcaaiback.validator.MensagemValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

    @Mock
    MensagemRepository mensagemRepository;
    @Mock
    ClienteService clienteService;
    @Mock
    AgendamentoService agendamentoService;
    @Mock
    MensagemValidator mensagemValidator;

    @InjectMocks
    MensagemService mensagemService;

    private Cliente cliente;
    private Agendamento agendamento;
    private Mensagem mensagem;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setTelefone("5581999999999");

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(cliente);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);

        mensagem = new Mensagem();
        mensagem.setId(1L);
        mensagem.setConteudo("Olá!");
        mensagem.setTipo(TipoDeMensagem.TEXTO);
        mensagem.setDataHora(LocalDateTime.now());
        mensagem.setCliente(cliente);
        mensagem.setAgendamento(agendamento);
    }

    @Test
    @DisplayName("registrar: registra mensagem com sucesso")
    void registrar_sucesso() {
        MensagemRequest request = new MensagemRequest("Olá!", TipoDeMensagem.TEXTO, 1L, 1L);

        when(clienteService.buscarEntidade(1L)).thenReturn(cliente);
        when(agendamentoService.buscarEntidade(1L)).thenReturn(agendamento);
        doNothing().when(mensagemValidator).validarConteudo(any());
        doNothing().when(mensagemValidator).validarClienteDoAgendamento(any(), any());
        doNothing().when(mensagemValidator).validarTipo(any());
        when(mensagemRepository.save(any())).thenReturn(mensagem);

        MensagemResponse response = mensagemService.registrar(request);

        assertThat(response.getConteudo()).isEqualTo("Olá!");
        assertThat(response.getClienteId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listarPorAgendamento: retorna mensagens existentes")
    void listarPorAgendamento_sucesso() {
        when(mensagemRepository.findByAgendamentoId(1L)).thenReturn(List.of(mensagem));

        List<MensagemResponse> result = mensagemService.listarPorAgendamento(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarPorAgendamento: lança EntidadeNaoEncontradaException quando vazio")
    void listarPorAgendamento_vazio() {
        when(mensagemRepository.findByAgendamentoId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> mensagemService.listarPorAgendamento(1L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("listarPorCliente: retorna mensagens do cliente")
    void listarPorCliente_sucesso() {
        when(mensagemRepository.findByClienteId(1L)).thenReturn(List.of(mensagem));

        List<MensagemResponse> result = mensagemService.listarPorCliente(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarPorCliente: lança EntidadeNaoEncontradaException quando vazio")
    void listarPorCliente_vazio() {
        when(mensagemRepository.findByClienteId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> mensagemService.listarPorCliente(1L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }
}
