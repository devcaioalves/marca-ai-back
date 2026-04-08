package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente findByTelefone(String telefone);

    List<Cliente> findByNome(String nome);
}
