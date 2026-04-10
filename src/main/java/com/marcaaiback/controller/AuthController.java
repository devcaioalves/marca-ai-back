package com.marcaaiback.controller;

import com.marcaaiback.jwt.JwtToken;
import com.marcaaiback.jwt.JwtUserDetailsService;
import com.marcaaiback.model.dto.admin.login.LoginRequest;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUserDetailsService detailsServices;
    private final AdminService adminService;


    @PostMapping
    public ResponseEntity<?> auth(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("Processo de autenticação por identificador {}", loginRequest.getLogin());
        try {
            // Busca admin por email ou matrícula
            Admin admin = adminService.buscarPorLogin(loginRequest.getLogin());

            // Gera token com email e role
            JwtToken token = detailsServices.getTokenAuthenticated(admin);

            return ResponseEntity.ok(token);
        } catch (EntityNotFoundException e) {
            log.warn("Tentativa de gerar token para um identificador inexistente '{}'", loginRequest.getLogin());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorMessage("Usuário não encontrado."));
        }
    }

}
