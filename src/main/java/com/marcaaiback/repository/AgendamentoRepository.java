package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.enuns.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // buscar agenda do dia para o painel do admin
    List<Agendamento> findByData(LocalDate data);

    // buscar por status (ex: todos CONFIRMADOS)
    List<Agendamento> findByStatusAgendamento(StatusAgendamento status);

    // buscar agendamentos de um cliente específico
    List<Agendamento> findByClienteId(Long clienteId);

    // verificar conflito de horário no mesmo horario disponivel
    boolean existsByHorarioDisponivelId(Long horarioDisponivelId);
}
