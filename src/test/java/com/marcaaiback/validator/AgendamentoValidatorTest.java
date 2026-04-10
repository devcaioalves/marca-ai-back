package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoValidatorTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private AgendamentoValidator agendamentoValidator;

    // ---- validarHorarioDisponivel ----

    @Test
    void deveLancarExcecaoQuandoHorarioIndisponivel() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setDisponivel(false);

        assertThatThrownBy(() -> agendamentoValidator.validarHorarioDisponivel(horario))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("não está mais disponível");
    }

    @Test
    void deveLancarExcecaoQuandoJaExisteAgendamentoNoHorario() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setId(1L);
        horario.setDisponivel(true);

        when(agendamentoRepository.existsByHorarioDisponivelId(1L)).thenReturn(true);

        assertThatThrownBy(() -> agendamentoValidator.validarHorarioDisponivel(horario))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um agendamento");
    }

    @Test
    void naoDeveLancarExcecaoQuandoHorarioDisponivel() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setId(1L);
        horario.setDisponivel(true);

        when(agendamentoRepository.existsByHorarioDisponivelId(1L)).thenReturn(false);

        assertThatCode(() -> agendamentoValidator.validarHorarioDisponivel(horario))
                .doesNotThrowAnyException();
    }

    // ---- validarCancelamento ----

    @Test
    void deveLancarExcecaoQuandoAgendamentoJaCancelado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);

        assertThatThrownBy(() -> agendamentoValidator.validarCancelamento(agendamento))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("já está cancelado");
    }

    @Test
    void naoDeveLancarExcecaoQuandoAgendamentoNaoCancelado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        assertThatCode(() -> agendamentoValidator.validarCancelamento(agendamento))
                .doesNotThrowAnyException();
    }

    // ---- validarRealizacao ----

    @Test
    void deveLancarExcecaoQuandoAgendamentoNaoConfirmado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);

        assertThatThrownBy(() -> agendamentoValidator.validarRealizacao(agendamento))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("confirmados");
    }

    @Test
    void naoDeveLancarExcecaoQuandoAgendamentoConfirmado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatusAgendamento(StatusAgendamento.CONFIRMADO);

        assertThatCode(() -> agendamentoValidator.validarRealizacao(agendamento))
                .doesNotThrowAnyException();
    }
}
