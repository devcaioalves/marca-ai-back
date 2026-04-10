package com.marcaaiback.service;

import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelRequest;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.HorarioDisponivelValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioDisponivelServiceTest {

    @Mock
    private HorarioDisponivelRepository horarioDisponivelRepository;

    @Mock
    private AdminService adminService;

    @Mock
    private HorarioDisponivelValidator horarioValidator;

    @InjectMocks
    private HorarioDisponivelService horarioDisponivelService;

    private HorarioDisponivel criarHorario() {
        HorarioDisponivel h = new HorarioDisponivel();
        h.setId(1L);
        h.setData(LocalDate.now());
        h.setHoraInicio(LocalTime.of(9, 0));
        h.setHoraFim(LocalTime.of(10, 0));
        h.setDisponivel(true);
        h.setAdmin(new Admin());
        return h;
    }

    private HorarioDisponivelRequest criarRequest() {
        HorarioDisponivelRequest r = new HorarioDisponivelRequest();
        r.setData(LocalDate.now());
        r.setHoraInicio(LocalTime.of(9, 0));
        r.setHoraFim(LocalTime.of(10, 0));
        return r;
    }

    @Test
    void deveCriarHorarioComSucesso() {
        when(adminService.buscarEntidade()).thenReturn(new Admin());
        when(horarioDisponivelRepository.save(any())).thenReturn(criarHorario());

        HorarioDisponivelResponse response = horarioDisponivelService.criar(criarRequest());

        assertThat(response.isDisponivel()).isTrue();
        verify(horarioValidator).validarDuplicidade(any(), any());
        verify(horarioValidator).validarIntervalo(any(), any());
    }

    @Test
    void deveBuscarHorarioPorId() {
        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(criarHorario()));

        HorarioDisponivelResponse response = horarioDisponivelService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void deveLancarExcecaoQuandoHorarioNaoEncontrado() {
        when(horarioDisponivelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioDisponivelService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveListarHorariosPorData() {
        when(horarioDisponivelRepository.findByData(any())).thenReturn(List.of(criarHorario()));

        List<HorarioDisponivelResponse> lista = horarioDisponivelService.listarPorData(LocalDate.now());

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveListarDisponiveisPorData() {
        when(horarioDisponivelRepository.findByDataAndDisponivel(any(), eq(true)))
                .thenReturn(List.of(criarHorario()));

        List<HorarioDisponivelResponse> lista = horarioDisponivelService.listarDisponiveisPorData(LocalDate.now());

        assertThat(lista).hasSize(1);
    }

    @Test
    void deveAlterarDisponibilidade() {
        HorarioDisponivel horario = criarHorario();
        horario.setDisponivel(true);
        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(horario));

        horarioDisponivelService.alterarDisponibilidade(1L);

        assertThat(horario.isDisponivel()).isFalse();
        verify(horarioDisponivelRepository).save(horario);
    }

    @Test
    void deveDeletarHorario() {
        HorarioDisponivel horario = criarHorario();
        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(horario));

        horarioDisponivelService.deletar(1L);

        verify(horarioValidator).validarExclusao(horario);
        verify(horarioDisponivelRepository).delete(horario);
    }

    @Test
    void deveSalvarHorario() {
        HorarioDisponivel horario = criarHorario();
        when(horarioDisponivelRepository.save(horario)).thenReturn(horario);

        HorarioDisponivel resultado = horarioDisponivelService.salvar(horario);

        assertThat(resultado).isEqualTo(horario);
    }
}
