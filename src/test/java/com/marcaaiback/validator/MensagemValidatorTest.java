package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MensagemValidatorTest {

    private final MensagemValidator mensagemValidator = new MensagemValidator();

    // ---- validarConteudo ----

    @Test
    void naoDeveLancarExcecaoQuandoConteudoValido() {
        assertThatCode(() -> mensagemValidator.validarConteudo("Olá, quero agendar"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoConteudoNulo() {
        assertThatThrownBy(() -> mensagemValidator.validarConteudo(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("não pode ser vazia");
    }

    @Test
    void deveLancarExcecaoQuandoConteudoVazio() {
        assertThatThrownBy(() -> mensagemValidator.validarConteudo("  "))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("não pode ser vazia");
    }

    @Test
    void deveLancarExcecaoQuandoConteudoMaiorQue500Caracteres() {
        String conteudoLongo = "a".repeat(501);
        assertThatThrownBy(() -> mensagemValidator.validarConteudo(conteudoLongo))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("500 caracteres");
    }

    // ---- validarClienteDoAgendamento ----

    @Test
    void naoDeveLancarExcecaoQuandoClienteCorresponde() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);

        assertThatCode(() -> mensagemValidator.validarClienteDoAgendamento(cliente, agendamento))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoPertenceAoAgendamento() {
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);

        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente2);

        assertThatThrownBy(() -> mensagemValidator.validarClienteDoAgendamento(cliente1, agendamento))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("não pertence");
    }

    // ---- validarTipo ----

    @Test
    void naoDeveLancarExcecaoQuandoTipoValido() {
        assertThatCode(() -> mensagemValidator.validarTipo(TipoDeMensagem.TEXTO))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoTipoNulo() {
        assertThatThrownBy(() -> mensagemValidator.validarTipo(null))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("obrigatório");
    }
}
