package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteValidatorTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteValidator clienteValidator;

    // ---- validarEPadronizarTelefone ----

    @Test
    void deveRetornarTelefoneFormatadoQuandoValido() {
        String resultado = clienteValidator.validarEPadronizarTelefone("(11) 99999-8888");
        assertThat(resultado).isEqualTo("11999998888");
    }

    @Test
    void deveLancarExcecaoQuandoTelefoneNulo() {
        assertThatThrownBy(() -> clienteValidator.validarEPadronizarTelefone(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("não pode ser vazio");
    }

    @Test
    void deveLancarExcecaoQuandoTelefoneVazio() {
        assertThatThrownBy(() -> clienteValidator.validarEPadronizarTelefone("   "))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("não pode ser vazio");
    }

    @Test
    void deveLancarExcecaoQuandoTelefoneMenosDe11Digitos() {
        assertThatThrownBy(() -> clienteValidator.validarEPadronizarTelefone("1199999888"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("11 dígitos");
    }

    @Test
    void deveLancarExcecaoQuandoTelefoneMaisDe11Digitos() {
        assertThatThrownBy(() -> clienteValidator.validarEPadronizarTelefone("119999988881"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("11 dígitos");
    }

    @Test
    void deveLancarExcecaoQuandoDDDInvalido() {
        assertThatThrownBy(() -> clienteValidator.validarEPadronizarTelefone("01999998888"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("DDD inválido");
    }

    @Test
    void deveLancarExcecaoQuandoNaoComecaComNove() {
        assertThatThrownBy(() -> clienteValidator.validarEPadronizarTelefone("11899998888"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Deve começar com 9");
    }

    // ---- validarTelefoneDuplicado ----

    @Test
    void deveLancarExcecaoQuandoTelefoneJaCadastrado() {
        when(clienteRepository.existsByTelefone("11999998888")).thenReturn(true);
        assertThatThrownBy(() -> clienteValidator.validarTelefoneDuplicado("11999998888"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um cliente");
    }

    @Test
    void naoDeveLancarExcecaoQuandoTelefoneNaoExiste() {
        when(clienteRepository.existsByTelefone("11999998888")).thenReturn(false);
        assertThatCode(() -> clienteValidator.validarTelefoneDuplicado("11999998888"))
                .doesNotThrowAnyException();
    }

    // ---- validarTelefoneDuplicadoNaAtualizacao ----

    @Test
    void naoDeveLancarExcecaoQuandoTelefoneNaoMudou() {
        assertThatCode(() ->
                clienteValidator.validarTelefoneDuplicadoNaAtualizacao("11999998888", "11999998888"))
                .doesNotThrowAnyException();
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveLancarExcecaoQuandoNovoTelefoneJaExiste() {
        when(clienteRepository.existsByTelefone("11988887777")).thenReturn(true);
        assertThatThrownBy(() ->
                clienteValidator.validarTelefoneDuplicadoNaAtualizacao("11988887777", "11999998888"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um cliente");
    }

    @Test
    void naoDeveLancarExcecaoQuandoNovoTelefoneNaoExiste() {
        when(clienteRepository.existsByTelefone("11988887777")).thenReturn(false);
        assertThatCode(() ->
                clienteValidator.validarTelefoneDuplicadoNaAtualizacao("11988887777", "11999998888"))
                .doesNotThrowAnyException();
    }
}
