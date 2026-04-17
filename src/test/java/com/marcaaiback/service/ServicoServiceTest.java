package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.dto.servico.ServicoRequest;
import com.marcaaiback.model.dto.servico.ServicoResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.ServicoRepository;
import com.marcaaiback.validator.ServicoValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock ServicoRepository servicoRepository;
    @Mock ServicoValidator servicoValidator;

    @InjectMocks ServicoService servicoService;

    private Servico servico;
    private ServicoRequest request;

    @BeforeEach
    void setUp() {
        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte");
        servico.setDescricao("Corte de cabelo");
        servico.setValor(new BigDecimal("30.00"));
        servico.setDuracao(30);
        servico.setAtivo(true);
        servico.setAgendamentos(new ArrayList<>());

        request = new ServicoRequest("Corte", "Corte de cabelo", new BigDecimal("30.00"), 30);
    }

    @Test
    @DisplayName("criar: sucesso com dados válidos")
    void criar_sucesso() {
        doNothing().when(servicoValidator).validarNome(any());
        doNothing().when(servicoValidator).validarValor(any());
        doNothing().when(servicoValidator).validarDuracao(any());
        doNothing().when(servicoValidator).validarDuplicidade(any());
        when(servicoRepository.save(any())).thenReturn(servico);

        ServicoResponse response = servicoService.criar(request);

        assertThat(response.getNome()).isEqualTo("Corte");
        assertThat(response.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("buscarPorId: retorna serviço existente")
    void buscarPorId_sucesso() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        ServicoResponse response = servicoService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId: lança EntityNotFoundException quando não encontrado")
    void buscarPorId_naoEncontrado() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("listarTodos: retorna todos os serviços ordenados")
    void listarTodos_sucesso() {
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(servico));

        List<ServicoResponse> result = servicoService.listarTodos();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarAtivos: retorna serviços ativos")
    void listarAtivos_sucesso() {
        when(servicoRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(servico));

        List<ServicoResponse> result = servicoService.listarAtivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isAtivo()).isTrue();
    }

    @Test
    @DisplayName("listarAtivos: lança EntidadeNaoEncontradaException quando nenhum ativo")
    void listarAtivos_semServicosAtivos() {
        when(servicoRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of());

        assertThatThrownBy(() -> servicoService.listarAtivos())
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("atualizar: atualiza serviço com sucesso")
    void atualizar_sucesso() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        doNothing().when(servicoValidator).validarNome(any());
        doNothing().when(servicoValidator).validarValor(any());
        doNothing().when(servicoValidator).validarDuracao(any());
        doNothing().when(servicoValidator).validarDuplicidadeNaAtualizacao(any(), any());
        when(servicoRepository.save(any())).thenReturn(servico);

        ServicoResponse response = servicoService.atualizar(1L, request);

        assertThat(response).isNotNull();
        verify(servicoRepository).save(any());
    }

    @Test
    @DisplayName("ativarDesativar: alterna status do serviço")
    void ativarDesativar_sucesso() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any())).thenReturn(servico);

        servicoService.ativarDesativar(1L);

        assertThat(servico.isAtivo()).isFalse();
        verify(servicoRepository).save(servico);
    }

    @Test
    @DisplayName("deletar: remove serviço sem agendamentos")
    void deletar_sucesso() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        doNothing().when(servicoRepository).delete(servico);

        assertThatCode(() -> servicoService.deletar(1L)).doesNotThrowAnyException();
        verify(servicoRepository).delete(servico);
    }

    @Test
    @DisplayName("deletar: lança OperacaoNaoPermitidaException com agendamentos vinculados")
    void deletar_comAgendamentos() {
        servico.setAgendamentos(List.of(new Agendamento()));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        assertThatThrownBy(() -> servicoService.deletar(1L))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("agendamentos vinculados");
    }
}
