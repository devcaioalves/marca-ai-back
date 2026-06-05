package com.marcaaiback.repository;

import com.marcaaiback.model.entity.ConversaWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversaWhatsappRepository extends JpaRepository<ConversaWhatsapp, Long> {

    Optional<ConversaWhatsapp> findByClienteTelefone(String telefone);

    boolean existsByClienteTelefone(String telefone);
}