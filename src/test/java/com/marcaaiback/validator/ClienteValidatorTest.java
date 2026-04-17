package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteValidatorTest {

    @Mock
    ClienteRepository clienteRepository;

    @InjectMocks
    ClienteValidator validator;

    @Test
    @DisplayName("validarEPadronizarTelefone: padroniza telefone válido")
    void padronizar_sucesso() {
        String resultado = validator.validarEPadronizarTelefone("81999999999");
        assertThat(resultado).isEqualTo("5581999999999");
    }

    @Test
    @DisplayName("validarEPadronizarTelefone: padroniza com formatação")
    void padronizar_comFormatacao() {
        String resultado = validator.validarEPadronizarTelefone("(81) 99999-9999");
        assertThat(resultado).isEqualTo("5581999999999");
    }

    @Test
    @DisplayName("validarEPadronizarTelefone: lança quando vazio")
    void padronizar_vazio() {
        assertThatThrownBy(() -> validator.validarEPadronizarTelefone(""))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarEPadronizarTelefone: lança quando menos de 11 dígitos")
    void padronizar_menosDe11Digitos() {
        assertThatThrownBy(() -> validator.validarEPadronizarTelefone("819999"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("11 dígitos");
    }

    @Test
    @DisplayName("validarEPadronizarTelefone: lança quando não começa com 9")
    void padronizar_semNoveAposDDD() {
        assertThatThrownBy(() -> validator.validarEPadronizarTelefone("81899999999"))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("9 após o DDD");
    }

    @Test
    @DisplayName("validarTelefoneDuplicado: lança quando já existe")
    void duplicado_existe() {
        when(clienteRepository.existsByTelefone("5581999999999")).thenReturn(true);

        assertThatThrownBy(() -> validator.validarTelefoneDuplicado("5581999999999"))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarTelefoneDuplicado: não lança quando não existe")
    void duplicado_naoExiste() {
        when(clienteRepository.existsByTelefone("5581999999999")).thenReturn(false);

        assertThatCode(() -> validator.validarTelefoneDuplicado("5581999999999"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarTelefoneDuplicadoNaAtualizacao: não lança quando telefone é o mesmo")
    void duplicadoAtualizacao_mesmoTelefone() {
        assertThatCode(() -> validator.validarTelefoneDuplicadoNaAtualizacao(
                "5581999999999", "5581999999999"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarTelefoneDuplicadoNaAtualizacao: lança quando telefone novo já existe")
    void duplicadoAtualizacao_novoTelefoneDuplicado() {
        when(clienteRepository.existsByTelefone("5581988888888")).thenReturn(true);

        assertThatThrownBy(() -> validator.validarTelefoneDuplicadoNaAtualizacao(
                "5581988888888", "5581999999999"
        )).isInstanceOf(RecursoDuplicadoException.class);
    }
}
