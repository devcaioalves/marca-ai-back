package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelRequest;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.HorarioDisponivelValidator;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioDisponivelServiceTest {

    @Mock HorarioDisponivelRepository horarioDisponivelRepository;
    @Mock AdminService adminService;
    @Mock HorarioDisponivelValidator horarioValidator;

    @InjectMocks HorarioDisponivelService horarioDisponivelService;

    private HorarioDisponivel horario;
    private Admin admin;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua A");
        endereco.setCidade("Recife");

        admin = Admin.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@email.com")
                .telefone("81999999999")
                .senha("encoded")
                .endereco(endereco)
                .build();

        horario = new HorarioDisponivel();
        horario.setId(1L);
        horario.setData(LocalDate.now().plusDays(2));
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFim(LocalTime.of(18, 0));
        horario.setDisponivel(true);
        horario.setAdmin(admin);
        horario.setAgendamentos(new ArrayList<>());
    }

    @Test
    @DisplayName("criar: sucesso com dados válidos")
    void criar_sucesso() {
        HorarioDisponivelRequest request = new HorarioDisponivelRequest(
                LocalDate.now().plusDays(2), LocalTime.of(8, 0), LocalTime.of(18, 0)
        );

        doNothing().when(horarioValidator).validarDuplicidade(any(), any());
        doNothing().when(horarioValidator).validarIntervalo(any(), any());
        doNothing().when(horarioValidator).validarHorarioPassado(any(), any());
        doNothing().when(horarioValidator).validarAntecedencia(any());
        doNothing().when(horarioValidator).validarConflitoHorario(any(), any(), any());
        when(adminService.buscarEntidade()).thenReturn(admin);
        when(horarioDisponivelRepository.save(any())).thenReturn(horario);

        HorarioDisponivelResponse response = horarioDisponivelService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.isDisponivel()).isTrue();
    }

    @Test
    @DisplayName("buscarPorId: retorna horário existente")
    void buscarPorId_sucesso() {
        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(horario));

        HorarioDisponivelResponse response = horarioDisponivelService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId: lança EntityNotFoundException quando não encontrado")
    void buscarPorId_naoEncontrado() {
        when(horarioDisponivelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioDisponivelService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("listarPorData: retorna horários da data")
    void listarPorData_sucesso() {
        when(horarioDisponivelRepository.findByDataWithAgendamentos(any())).thenReturn(List.of(horario));

        List<HorarioDisponivelResponse> result = horarioDisponivelService.listarPorData(LocalDate.now().plusDays(2));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarPorData: lança EntidadeNaoEncontradaException quando vazio")
    void listarPorData_vazio() {
        when(horarioDisponivelRepository.findByDataWithAgendamentos(any())).thenReturn(List.of());

        assertThatThrownBy(() -> horarioDisponivelService.listarPorData(LocalDate.now()))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("listarDisponiveisPorData: retorna slots livres")
    void listarDisponiveisPorData_sucesso() {
        when(horarioDisponivelRepository.findByDataWithAgendamentos(any())).thenReturn(List.of(horario));

        List<HorarioDisponivelResponse> result = horarioDisponivelService
                .listarDisponiveisPorData(LocalDate.now().plusDays(2));

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("listarDisponiveisPorData: lança quando nenhum horário na data")
    void listarDisponiveisPorData_semHorarios() {
        when(horarioDisponivelRepository.findByDataWithAgendamentos(any())).thenReturn(List.of());

        assertThatThrownBy(() -> horarioDisponivelService.listarDisponiveisPorData(LocalDate.now()))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("listarDisponiveisPorData: lança quando todos slots ocupados")
    void listarDisponiveisPorData_todosOcupados() {
        Agendamento ag = new Agendamento();
        ag.setHoraInicio(LocalTime.of(8, 0));
        ag.setHoraFim(LocalTime.of(18, 0));
        ag.setStatusAgendamento(StatusAgendamento.CONFIRMADO);
        horario.setAgendamentos(List.of(ag));

        when(horarioDisponivelRepository.findByDataWithAgendamentos(any())).thenReturn(List.of(horario));

        assertThatThrownBy(() -> horarioDisponivelService.listarDisponiveisPorData(LocalDate.now().plusDays(2)))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    @DisplayName("alterarDisponibilidade: atualiza horário com sucesso")
    void alterarDisponibilidade_sucesso() {
        HorarioDisponivelRequest request = new HorarioDisponivelRequest(
                LocalDate.now().plusDays(3), LocalTime.of(9, 0), LocalTime.of(17, 0)
        );

        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(horario));
        doNothing().when(horarioValidator).validarDuplicidadeAtualizacao(any(), any(), any());
        doNothing().when(horarioValidator).validarIntervalo(any(), any());
        doNothing().when(horarioValidator).validarHorarioPassado(any(), any());
        doNothing().when(horarioValidator).validarAntecedencia(any());
        doNothing().when(horarioValidator).validarConflitoAtualizacao(any(), any(), any(), any());
        when(horarioDisponivelRepository.save(any())).thenReturn(horario);

        HorarioDisponivelResponse response = horarioDisponivelService.alterarDisponibilidade(1L, request);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("alterarDisponibilidade: lança quando agendamento fora do novo intervalo")
    void alterarDisponibilidade_agendamentoForaDoIntervalo() {
        Agendamento ag = new Agendamento();
        ag.setStatusAgendamento(StatusAgendamento.AGENDADO);
        ag.setHoraInicio(LocalTime.of(7, 0));
        ag.setHoraFim(LocalTime.of(7, 30));
        horario.setAgendamentos(List.of(ag));

        HorarioDisponivelRequest request = new HorarioDisponivelRequest(
                LocalDate.now().plusDays(2), LocalTime.of(8, 0), LocalTime.of(18, 0)
        );

        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(horario));
        doNothing().when(horarioValidator).validarDuplicidadeAtualizacao(any(), any(), any());
        doNothing().when(horarioValidator).validarIntervalo(any(), any());
        doNothing().when(horarioValidator).validarHorarioPassado(any(), any());
        doNothing().when(horarioValidator).validarAntecedencia(any());
        doNothing().when(horarioValidator).validarConflitoAtualizacao(any(), any(), any(), any());

        assertThatThrownBy(() -> horarioDisponivelService.alterarDisponibilidade(1L, request))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("fora do novo intervalo");
    }

    @Test
    @DisplayName("deletar: remove horário sem agendamentos ativos")
    void deletar_sucesso() {
        when(horarioDisponivelRepository.findById(1L)).thenReturn(Optional.of(horario));
        doNothing().when(horarioValidator).validarExclusao(horario);
        doNothing().when(horarioDisponivelRepository).delete(horario);

        assertThatCode(() -> horarioDisponivelService.deletar(1L)).doesNotThrowAnyException();
        verify(horarioDisponivelRepository).delete(horario);
    }
}