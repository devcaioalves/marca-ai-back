package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Servico findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome);

    List<Servico> findAllByOrderByNomeAsc();

    List<Servico> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

}
