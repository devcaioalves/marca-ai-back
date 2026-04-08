package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.model.enuns.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Long> {

    List<HorarioDisponivel> findAllByData(LocalDate data);

    boolean existsByData(LocalDate data);

}
