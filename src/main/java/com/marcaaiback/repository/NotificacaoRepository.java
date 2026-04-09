package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Notificacao;
import com.marcaaiback.model.enuns.StatusNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // notificações de um agendamento específico
    List<Notificacao> findByAgendamentoId(Long agendamentoId);

    // notificações por status (ex: todas ENVIADAS ainda não LIDAS)
    List<Notificacao> findByStatusNotificacao(StatusNotificacao status);
}