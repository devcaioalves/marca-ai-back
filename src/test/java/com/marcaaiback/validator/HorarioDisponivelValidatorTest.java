package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioDisponivelValidatorTest {

    @Mock
    private HorarioDisponivelRepository horarioDisponivelRepository;

    @InjectMocks
    private HorarioDisponivelValidator horarioDisponivelValidator;

    // ---- validarDuplicidade ----

    @Test
    void deveLancarExcecaoQuandoHorarioDuplicado() {
        LocalDate data = LocalDate.now();
        LocalTime hora = LocalTime.of(9, 0);
        when(horarioDisponivelRepository.existsByDataAndHoraInicio(data, hora)).thenReturn(true);

        assertThatThrownBy(() -> horarioDisponivelValidator.validarDuplicidade(data, hora))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um horário");
    }

    @Test
    void naoDeveLancarExcecaoQuandoHorarioNaoExiste() {
        LocalDate data = LocalDate.now();
        LocalTime hora = LocalTime.of(9, 0);
        when(horarioDisponivelRepository.existsByDataAndHoraInicio(data, hora)).thenReturn(false);

        assertThatCode(() -> horarioDisponivelValidator.validarDuplicidade(data, hora))
                .doesNotThrowAnyException();
    }

    // ---- validarIntervalo ----

    @Test
    void naoDeveLancarExcecaoQuandoIntervaloValido() {
        assertThatCode(() -> horarioDisponivelValidator.validarIntervalo(
                LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoHoraFimAnteriorAInicio() {
        assertThatThrownBy(() -> horarioDisponivelValidator.validarIntervalo(
                LocalTime.of(10, 0), LocalTime.of(9, 0)))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("hora fim deve ser maior");
    }

    @Test
    void deveLancarExcecaoQuandoHoraFimIgualAInicio() {
        assertThatThrownBy(() -> horarioDisponivelValidator.validarIntervalo(
                LocalTime.of(9, 0), LocalTime.of(9, 0)))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("hora fim deve ser maior");
    }

    // ---- validarExclusao ----

    @Test
    void deveLancarExcecaoQuandoHorarioIndisponivelNaExclusao() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setDisponivel(false);

        assertThatThrownBy(() -> horarioDisponivelValidator.validarExclusao(horario))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("vinculado a um agendamento");
    }

    @Test
    void naoDeveLancarExcecaoQuandoHorarioDisponivelNaExclusao() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setDisponivel(true);

        assertThatCode(() -> horarioDisponivelValidator.validarExclusao(horario))
                .doesNotThrowAnyException();
    }
}
