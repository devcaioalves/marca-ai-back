package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Servico> findByNomeIgnoreCase(String nome);

    // verifica duplicidade de nome ignorando o próprio registro (usado no update)
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    // listagem geral ordenada (painel admin)
    List<Servico> findAllByOrderByNomeAsc();

    // listagem apenas ativos ordenada (chatbot e cliente)
    List<Servico> findByAtivoTrueOrderByNomeAsc();
}
