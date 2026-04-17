package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioDisponivelValidatorTest {

    @Mock HorarioDisponivelRepository horarioDisponivelRepository;

    @InjectMocks HorarioDisponivelValidator validator;

    @Test
    @DisplayName("validarDuplicidade: lança quando já existe horário na data e hora")
    void validarDuplicidade_existe() {
        when(horarioDisponivelRepository.existsByDataAndHoraInicio(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> validator.validarDuplicidade(LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarDuplicidade: não lança quando não existe")
    void validarDuplicidade_naoExiste() {
        when(horarioDisponivelRepository.existsByDataAndHoraInicio(any(), any())).thenReturn(false);

        assertThatCode(() -> validator.validarDuplicidade(LocalDate.now(), LocalTime.of(9, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarDuplicidadeAtualizacao: lança quando outro horário tem mesma data e hora")
    void validarDuplicidadeAtualizacao_conflito() {
        HorarioDisponivel outro = new HorarioDisponivel();
        outro.setId(2L);

        when(horarioDisponivelRepository.findByDataAndHoraInicio(any(), any()))
                .thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> validator.validarDuplicidadeAtualizacao(1L, LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarDuplicidadeAtualizacao: não lança quando é o mesmo horário")
    void validarDuplicidadeAtualizacao_mesmoHorario() {
        HorarioDisponivel mesmo = new HorarioDisponivel();
        mesmo.setId(1L);

        when(horarioDisponivelRepository.findByDataAndHoraInicio(any(), any()))
                .thenReturn(Optional.of(mesmo));

        assertThatCode(() -> validator.validarDuplicidadeAtualizacao(1L, LocalDate.now(), LocalTime.of(9, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarIntervalo: lança quando horaFim igual a horaInicio")
    void validarIntervalo_igual() {
        assertThatThrownBy(() -> validator.validarIntervalo(LocalTime.of(9, 0), LocalTime.of(9, 0)))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarIntervalo: lança quando horaFim antes de horaInicio")
    void validarIntervalo_fimAntes() {
        assertThatThrownBy(() -> validator.validarIntervalo(LocalTime.of(10, 0), LocalTime.of(9, 0)))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
    }

    @Test
    @DisplayName("validarIntervalo: não lança com intervalo válido")
    void validarIntervalo_valido() {
        assertThatCode(() -> validator.validarIntervalo(LocalTime.of(9, 0), LocalTime.of(18, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarAntecedencia: lança quando data é hoje")
    void validarAntecedencia_hoje() {
        assertThatThrownBy(() -> validator.validarAntecedencia(LocalDate.now()))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("1 dia");
    }

    @Test
    @DisplayName("validarAntecedencia: não lança quando data é amanhã")
    void validarAntecedencia_amanha() {
        assertThatCode(() -> validator.validarAntecedencia(LocalDate.now().plusDays(1)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarConflitoHorario: lança quando há sobreposição")
    void validarConflito_sobreposicao() {
        HorarioDisponivel existente = new HorarioDisponivel();
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFim(LocalTime.of(12, 0));

        when(horarioDisponivelRepository.findByData(any())).thenReturn(List.of(existente));

        assertThatThrownBy(() -> validator.validarConflitoHorario(
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(13, 0)
        )).isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("validarConflitoHorario: não lança sem sobreposição")
    void validarConflito_semSobreposicao() {
        HorarioDisponivel existente = new HorarioDisponivel();
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFim(LocalTime.of(12, 0));

        when(horarioDisponivelRepository.findByData(any())).thenReturn(List.of(existente));

        assertThatCode(() -> validator.validarConflitoHorario(
                LocalDate.now().plusDays(1), LocalTime.of(13, 0), LocalTime.of(18, 0)
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarExclusao: lança quando há agendamentos ativos")
    void validarExclusao_comAgendamentos() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.AGENDADO);

        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setAgendamentos(List.of(ag));

        assertThatThrownBy(() -> validator.validarExclusao(horario))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("agendamentos vinculados");
    }

    @Test
    @DisplayName("validarExclusao: não lança quando agendamentos são cancelados")
    void validarExclusao_agendamentosCancelados() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.CANCELADO);

        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setAgendamentos(List.of(ag));

        assertThatCode(() -> validator.validarExclusao(horario)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarExclusao: não lança quando não há agendamentos")
    void validarExclusao_semAgendamentos() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setAgendamentos(new ArrayList<>());

        assertThatCode(() -> validator.validarExclusao(horario)).doesNotThrowAnyException();
    }
}
