package com.marcaaiback.service;

import com.marcaaiback.model.dto.servico.ServicoRequest;
import com.marcaaiback.model.dto.servico.ServicoResponse;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.ServicoRepository;
import com.marcaaiback.validator.ServicoValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoValidator servicoValidator;

    @InjectMocks
    private ServicoService servicoService;

    private Servico criarServico() {
        Servico s = new Servico();
        s.setId(1L);
        s.setNome("Corte");
        s.setDescricao("Corte simples");
        s.setValor(new BigDecimal("50.00"));
        s.setDuracao(30);
        s.setAtivo(true);
        return s;
    }

    private ServicoRequest criarRequest() {
        ServicoRequest r = new ServicoRequest();
        r.setNome("Corte");
        r.setDescricao("Corte simples");
        r.setValor(new BigDecimal("50.00"));
        r.setDuracao(30);
        return r;
    }

    @Test
    void deveCriarServicoComSucesso() {
        ServicoRequest request = criarRequest();
        Servico servico = criarServico();
        when(servicoRepository.save(any())).thenReturn(servico);

        ServicoResponse response = servicoService.criar(request);

        assertThat(response.getNome()).isEqualTo("Corte");
        assertThat(response.isAtivo()).isTrue();
        verify(servicoValidator).validarNome("Corte");
        verify(servicoValidator).validarDuplicidade("Corte");
    }

    @Test
    void deveBuscarServicoPorId() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(criarServico()));

        ServicoResponse response = servicoService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoEncontrado() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void deveListarTodosServicos() {
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(criarServico()));

        List<ServicoResponse> lista = servicoService.listarTodos();

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveListarServicosAtivos() {
        when(servicoRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(criarServico()));

        List<ServicoResponse> lista = servicoService.listarAtivos();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).isAtivo()).isTrue();
    }

    @Test
    void deveAtualizarServico() {
        Servico servico = criarServico();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any())).thenReturn(servico);

        ServicoResponse response = servicoService.atualizar(1L, criarRequest());

        assertThat(response.getNome()).isEqualTo("Corte");
        verify(servicoValidator).validarDuplicidadeNaAtualizacao("Corte", 1L);
    }

    @Test
    void deveAtivarDesativarServico() {
        Servico servico = criarServico();
        servico.setAtivo(true);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        servicoService.ativarDesativar(1L);

        assertThat(servico.isAtivo()).isFalse();
        verify(servicoRepository).save(servico);
    }

    @Test
    void deveDeletarServico() {
        Servico servico = criarServico();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        servicoService.deletar(1L);

        verify(servicoRepository).delete(servico);
    }
}
