package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoValidatorTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoValidator servicoValidator;

    // ---- validarNome ----

    @Test
    void naoDeveLancarExcecaoQuandoNomeValido() {
        assertThatCode(() -> servicoValidator.validarNome("Corte de cabelo"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoNomeNulo() {
        assertThatThrownBy(() -> servicoValidator.validarNome(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        assertThatThrownBy(() -> servicoValidator.validarNome("  "))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("obrigatório");
    }

    // ---- validarValor ----

    @Test
    void naoDeveLancarExcecaoQuandoValorValido() {
        assertThatCode(() -> servicoValidator.validarValor(new BigDecimal("50.00")))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoValorNulo() {
        assertThatThrownBy(() -> servicoValidator.validarValor(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void deveLancarExcecaoQuandoValorZero() {
        assertThatThrownBy(() -> servicoValidator.validarValor(BigDecimal.ZERO))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void deveLancarExcecaoQuandoValorNegativo() {
        assertThatThrownBy(() -> servicoValidator.validarValor(new BigDecimal("-10.00")))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("maior que zero");
    }

    // ---- validarDuracao ----

    @Test
    void naoDeveLancarExcecaoQuandoDuracaoValida() {
        assertThatCode(() -> servicoValidator.validarDuracao(30))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoDuracaoNula() {
        assertThatThrownBy(() -> servicoValidator.validarDuracao(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void deveLancarExcecaoQuandoDuracaoZero() {
        assertThatThrownBy(() -> servicoValidator.validarDuracao(0))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("maior que zero");
    }

    // ---- validarDuplicidade ----

    @Test
    void deveLancarExcecaoQuandoNomeJaExiste() {
        when(servicoRepository.existsByNomeIgnoreCase("Corte")).thenReturn(true);
        assertThatThrownBy(() -> servicoValidator.validarDuplicidade("Corte"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um serviço");
    }

    @Test
    void naoDeveLancarExcecaoQuandoNomeNaoExiste() {
        when(servicoRepository.existsByNomeIgnoreCase("Corte")).thenReturn(false);
        assertThatCode(() -> servicoValidator.validarDuplicidade("Corte"))
                .doesNotThrowAnyException();
    }

    // ---- validarDuplicidadeNaAtualizacao ----

    @Test
    void deveLancarExcecaoQuandoNomeDuplicadoNaAtualizacao() {
        when(servicoRepository.existsByNomeIgnoreCaseAndIdNot("Corte", 1L)).thenReturn(true);
        assertThatThrownBy(() -> servicoValidator.validarDuplicidadeNaAtualizacao("Corte", 1L))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um serviço");
    }

    @Test
    void naoDeveLancarExcecaoQuandoNomeNaoConflita() {
        when(servicoRepository.existsByNomeIgnoreCaseAndIdNot("Corte", 1L)).thenReturn(false);
        assertThatCode(() -> servicoValidator.validarDuplicidadeNaAtualizacao("Corte", 1L))
                .doesNotThrowAnyException();
    }
}
