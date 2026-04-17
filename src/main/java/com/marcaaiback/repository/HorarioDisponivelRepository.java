package com.marcaaiback.repository;

import com.marcaaiback.model.entity.HorarioDisponivel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT h FROM HorarioDisponivel h
    LEFT JOIN FETCH h.agendamentos
    WHERE h.id = :id
""")
    Optional<HorarioDisponivel> buscarComLock(@Param("id") Long id);

    @Query("SELECT h FROM HorarioDisponivel h LEFT JOIN FETCH h.agendamentos WHERE h.data = :data")
    List<HorarioDisponivel> findByDataWithAgendamentos(@Param("data") LocalDate data);

    // buscar horários disponíveis por data (cliente escolhe o dia)
    List<HorarioDisponivel> findByDataAndDisponivel(LocalDate data, boolean disponivel);

    // buscar todos horários de uma data independente de disponibilidade (painel admin)
    List<HorarioDisponivel> findByData(LocalDate data);

    // verificar conflito ao cadastrar novo horário
    boolean existsByDataAndHoraInicio(LocalDate data, LocalTime horaInicio);

    // buscar horarios de uma data e de um horario iniciante específico
    Optional<HorarioDisponivel> findByDataAndHoraInicio(LocalDate data, LocalTime horaInicio);
}
