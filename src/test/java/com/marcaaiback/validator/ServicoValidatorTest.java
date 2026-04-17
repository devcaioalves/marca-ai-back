package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.repository.*;
import org.junit.jupiter.api.DisplayName;
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

    @Mock ServicoRepository servicoRepository;

    @InjectMocks ServicoValidator validator;

    @Test
    @DisplayName("validarNome: não lança com nome válido")
    void validarNome_valido() {
        assertThatCode(() -> validator.validarNome("Corte")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarNome: lança quando nome vazio")
    void validarNome_vazio() {
        assertThatThrownBy(() -> validator.validarNome(""))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarNome: lança quando nome nulo")
    void validarNome_nulo() {
        assertThatThrownBy(() -> validator.validarNome(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarValor: não lança com valor positivo")
    void validarValor_valido() {
        assertThatCode(() -> validator.validarValor(new BigDecimal("10.00"))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarValor: lança quando valor negativo")
    void validarValor_negativo() {
        assertThatThrownBy(() -> validator.validarValor(new BigDecimal("-1.00")))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarValor: lança quando valor nulo")
    void validarValor_nulo() {
        assertThatThrownBy(() -> validator.validarValor(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarDuracao: não lança com duração positiva")
    void validarDuracao_valida() {
        assertThatCode(() -> validator.validarDuracao(30)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarDuracao: lança quando duração zero")
    void validarDuracao_zero() {
        assertThatThrownBy(() -> validator.validarDuracao(0))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarDuplicidade: lança quando nome já existe")
    void validarDuplicidade_existe() {
        when(servicoRepository.existsByNomeIgnoreCase("Corte")).thenReturn(true);

        assertThatThrownBy(() -> validator.validarDuplicidade("Corte"))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarDuplicidadeNaAtualizacao: lança quando outro serviço tem o mesmo nome")
    void validarDuplicidadeAtualizacao_existe() {
        when(servicoRepository.existsByNomeIgnoreCaseAndIdNot("Corte", 1L)).thenReturn(true);

        assertThatThrownBy(() -> validator.validarDuplicidadeNaAtualizacao("Corte", 1L))
                .isInstanceOf(RecursoDuplicadoException.class);
    }
}
