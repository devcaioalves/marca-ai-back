package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findAllByDataAgendamento(LocalDate data);

    List<Agendamento> findAllByCliente(Cliente cliente);

    List<Agendamento> findByDataAgendamento(LocalDate data);

    List<Agendamento> findByClienteAndDataAgendamento(Cliente cliente, LocalDate data);

    List<Agendamento> findAllAgendamentosByStatusAgendamento(StatusAgendamento statusAgendamento);

    List<Agendamento> findAllByOrderByDataAgendamentoAscHoraInicioAsc();

    boolean existsByServico(Servico servico);

}
