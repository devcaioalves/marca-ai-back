package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MensagemValidatorTest {

    @InjectMocks
    MensagemValidator validator;

    @Test
    @DisplayName("validarConteudo: não lança com conteúdo válido")
    void validarConteudo_valido() {
        assertThatCode(() -> validator.validarConteudo("Olá!")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarConteudo: lança quando vazio")
    void validarConteudo_vazio() {
        assertThatThrownBy(() -> validator.validarConteudo(""))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarConteudo: lança quando nulo")
    void validarConteudo_nulo() {
        assertThatThrownBy(() -> validator.validarConteudo(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarConteudo: lança quando excede 500 caracteres")
    void validarConteudo_muitoLongo() {
        String longo = "a".repeat(501);
        assertThatThrownBy(() -> validator.validarConteudo(longo))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("500 caracteres");
    }

    @Test
    @DisplayName("validarClienteDoAgendamento: lança quando cliente não pertence ao agendamento")
    void validarCliente_errado() {
        Cliente c1 = new Cliente();
        c1.setId(1L);
        Cliente c2 = new Cliente();
        c2.setId(2L);
        Agendamento ag = new Agendamento();
        ag.setCliente(c2);

        assertThatThrownBy(() -> validator.validarClienteDoAgendamento(c1, ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarClienteDoAgendamento: não lança quando cliente correto")
    void validarCliente_correto() {
        Cliente c1 = new Cliente();
        c1.setId(1L);
        Agendamento ag = new Agendamento();
        ag.setCliente(c1);

        assertThatCode(() -> validator.validarClienteDoAgendamento(c1, ag))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarTipo: lança quando tipo nulo")
    void validarTipo_nulo() {
        assertThatThrownBy(() -> validator.validarTipo(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarTipo: não lança com tipo válido")
    void validarTipo_valido() {
        assertThatCode(() -> validator.validarTipo(TipoDeMensagem.TEXTO))
                .doesNotThrowAnyException();
    }
}
