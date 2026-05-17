package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // já tinha — correto para evitar cliente duplicado
    boolean existsByTelefone(String telefone);

    // buscar cliente pelo telefone (chatbot vai precisar disso)
    Optional<Cliente> findByTelefone(String telefone);

    List<Cliente> findByNomeContainingIgnoreCaseOrTelefoneContaining(String termo, String termo1);
}
