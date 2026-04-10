package com.marcaaiback.repository;

import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.SenhaResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SenhaResetTokenRepository extends JpaRepository<SenhaResetToken, Long> {
    Optional<SenhaResetToken> findByToken(String token);
    Optional<SenhaResetToken> findByAdmin(Admin admin);
}
