package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Long> {

    // buscar horários disponíveis por data (cliente escolhe o dia)
    List<HorarioDisponivel> findByDataAndDisponivel(LocalDate data, boolean disponivel);

    // buscar todos horários de uma data independente de disponibilidade (painel admin)
    List<HorarioDisponivel> findByData(LocalDate data);

    // verificar conflito ao cadastrar novo horário
    boolean existsByDataAndHoraInicio(LocalDate data, LocalTime horaInicio);
}
