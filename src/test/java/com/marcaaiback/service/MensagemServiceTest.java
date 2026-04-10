package com.marcaaiback.service;

import com.marcaaiback.model.dto.mensagem.MensagemRequest;
import com.marcaaiback.model.dto.mensagem.MensagemResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Mensagem;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import com.marcaaiback.repository.MensagemRepository;
import com.marcaaiback.validator.MensagemValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

    @Mock private MensagemRepository mensagemRepository;
    @Mock private ClienteService clienteService;
    @Mock private AgendamentoService agendamentoService;
    @Mock private MensagemValidator mensagemValidator;

    @InjectMocks
    private MensagemService mensagemService;

    private Cliente criarCliente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNome("João");
        return c;
    }

    private Agendamento criarAgendamento() {
        Agendamento a = new Agendamento();
        a.setId(1L);
        a.setCliente(criarCliente());
        a.setStatusAgendamento(StatusAgendamento.CONFIRMADO);
        return a;
    }

    private Mensagem criarMensagem() {
        Mensagem m = new Mensagem();
        m.setId(1L);
        m.setConteudo("Olá");
        m.setTipo(TipoDeMensagem.TEXTO);
        m.setDataHora(LocalDateTime.now());
        m.setCliente(criarCliente());
        m.setAgendamento(criarAgendamento());
        return m;
    }

    @Test
    void deveRegistrarMensagemComSucesso() {
        MensagemRequest request = new MensagemRequest("Olá", TipoDeMensagem.TEXTO, 1L, 1L);

        when(clienteService.buscarEntidade(1L)).thenReturn(criarCliente());
        when(agendamentoService.buscarEntidade(1L)).thenReturn(criarAgendamento());
        when(mensagemRepository.save(any())).thenReturn(criarMensagem());

        MensagemResponse response = mensagemService.registrar(request);

        assertThat(response.getConteudo()).isEqualTo("Olá");
        assertThat(response.getTipo()).isEqualTo(TipoDeMensagem.TEXTO);
        verify(mensagemValidator).validarConteudo("Olá");
        verify(mensagemValidator).validarClienteDoAgendamento(any(), any());
        verify(mensagemValidator).validarTipo(TipoDeMensagem.TEXTO);
    }

    @Test
    void deveListarMensagensPorAgendamento() {
        when(mensagemRepository.findByAgendamentoId(1L)).thenReturn(List.of(criarMensagem()));

        List<MensagemResponse> lista = mensagemService.listarPorAgendamento(1L);

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveListarMensagensPorCliente() {
        when(mensagemRepository.findByClienteId(1L)).thenReturn(List.of(criarMensagem()));

        List<MensagemResponse> lista = mensagemService.listarPorCliente(1L);

        assertThat(lista).hasSize(1);
    }
}
