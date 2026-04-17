package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AgendamentoValidatorTest {

    @Mock
    AgendamentoRepository agendamentoRepository;

    @InjectMocks
    AgendamentoValidator validator;

    private HorarioDisponivel horario;

    @BeforeEach
    void setUp() {
        horario = new HorarioDisponivel();
        horario.setId(1L);
        horario.setData(LocalDate.now().plusDays(2));
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFim(LocalTime.of(18, 0));
        horario.setAgendamentos(new ArrayList<>());
    }

    @Test
    @DisplayName("validarHorarioDentroDoIntervalo: não lança quando dentro do intervalo")
    void validarHorario_dentroDointervalo() {
        assertThatCode(() -> validator.validarHorarioDentroDoIntervalo(
                horario, LocalTime.of(9, 0), LocalTime.of(9, 30)
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarHorarioDentroDoIntervalo: lança quando início antes do horário")
    void validarHorario_inicioAntes() {
        assertThatThrownBy(() -> validator.validarHorarioDentroDoIntervalo(
                horario, LocalTime.of(7, 0), LocalTime.of(9, 0)
        )).isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarHorarioDentroDoIntervalo: lança quando fim depois do horário")
    void validarHorario_fimDepois() {
        assertThatThrownBy(() -> validator.validarHorarioDentroDoIntervalo(
                horario, LocalTime.of(17, 0), LocalTime.of(19, 0)
        )).isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarCancelamento: lança quando já cancelado")
    void validarCancelamento_jaCancelado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.CANCELADO, 2);

        assertThatThrownBy(() -> validator.validarCancelamento(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("já está cancelado");
    }

    @Test
    @DisplayName("validarCancelamento: lança quando realizado")
    void validarCancelamento_realizado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.REALIZADO, 2);

        assertThatThrownBy(() -> validator.validarCancelamento(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("realizados não podem ser cancelados");
    }

    @Test
    @DisplayName("validarCancelamento: lança quando menos de 1 hora de antecedência")
    void validarCancelamento_semAntecedencia() {
        Agendamento ag = criarAgendamento(StatusAgendamento.AGENDADO, 0);

        assertThatThrownBy(() -> validator.validarCancelamento(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("1 hora");
    }

    @Test
    @DisplayName("validarRealizacao: lança quando não confirmado")
    void validarRealizacao_naoConfirmado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.AGENDADO, 2);

        assertThatThrownBy(() -> validator.validarRealizacao(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("confirmados");
    }

    @Test
    @DisplayName("validarRealizacao: não lança quando confirmado")
    void validarRealizacao_confirmado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.CONFIRMADO, 2);

        assertThatCode(() -> validator.validarRealizacao(ag)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarRemarcacao: lança quando cancelado")
    void validarRemarcacao_cancelado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.CANCELADO, 2);

        assertThatThrownBy(() -> validator.validarRemarcacao(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarRemarcacao: lança quando realizado")
    void validarRemarcacao_realizado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.REALIZADO, 2);

        assertThatThrownBy(() -> validator.validarRemarcacao(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarConfirmacao: lança quando já confirmado")
    void validarConfirmacao_jaConfirmado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.CONFIRMADO, 2);

        assertThatThrownBy(() -> validator.validarConfirmacao(ag))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarConfirmacao: lança quando cancelado")
    void validarConfirmacao_cancelado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.CANCELADO, 2);

        assertThatThrownBy(() -> validator.validarConfirmacao(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarConfirmacao: lança quando realizado")
    void validarConfirmacao_realizado() {
        Agendamento ag = criarAgendamento(StatusAgendamento.REALIZADO, 2);

        assertThatThrownBy(() -> validator.validarConfirmacao(ag))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarAntecedenciaMinima: lança quando menos de 2 horas")
    void validarAntecedencia_insuficiente() {
        assertThatThrownBy(() -> validator.validarAntecedenciaMinima(
                LocalDate.now(), LocalTime.now().plusMinutes(30)
        )).isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("2 horas");
    }

    @Test
    @DisplayName("validarAntecedenciaMinima: não lança com antecedência suficiente")
    void validarAntecedencia_suficiente() {
        assertThatCode(() -> validator.validarAntecedenciaMinima(
                LocalDate.now().plusDays(1), LocalTime.of(10, 0)
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarConflito: lança quando há sobreposição de horário")
    void validarConflito_comConflito() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.AGENDADO);
        ag.setHoraInicio(LocalTime.of(9, 0));
        ag.setHoraFim(LocalTime.of(9, 30));
        horario.setAgendamentos(List.of(ag));

        assertThatThrownBy(() -> validator.validarConflito(horario, LocalTime.of(9, 0), LocalTime.of(9, 30)))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    @DisplayName("validarConflito: não lança quando agendamento está cancelado")
    void validarConflito_canceladoIgnorado() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.CANCELADO);
        ag.setHoraInicio(LocalTime.of(9, 0));
        ag.setHoraFim(LocalTime.of(9, 30));
        horario.setAgendamentos(List.of(ag));

        assertThatCode(() -> validator.validarConflito(horario, LocalTime.of(9, 0), LocalTime.of(9, 30)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarServicoAtivo: lança quando serviço inativo")
    void validarServicoAtivo_inativo() {
        Servico servico = new Servico();
        servico.setAtivo(false);

        assertThatThrownBy(() -> validator.validarServicoAtivo(servico))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarServicoAtivo: não lança quando ativo")
    void validarServicoAtivo_ativo() {
        Servico servico = new Servico();
        servico.setAtivo(true);

        assertThatCode(() -> validator.validarServicoAtivo(servico)).doesNotThrowAnyException();
    }

    private Agendamento criarAgendamento(StatusAgendamento status, int horasAfrente) {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(status);
        LocalDateTime futuro = LocalDateTime.now().plusHours(horasAfrente);
        ag.setData(futuro.toLocalDate());
        ag.setHoraInicio(futuro.toLocalTime());
        return ag;
    }
}
