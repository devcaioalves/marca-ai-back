package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    // histórico de mensagens de um agendamento
    List<Mensagem> findByAgendamentoId(Long agendamentoId);

    // histórico de mensagens de um cliente
    List<Mensagem> findByClienteId(Long clienteId);
}
