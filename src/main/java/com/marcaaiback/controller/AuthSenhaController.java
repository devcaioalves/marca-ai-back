package com.marcaaiback.controller;

import com.marcaaiback.model.dto.admin.senha.EsqueciSenhaRequest;
import com.marcaaiback.model.dto.admin.senha.RedefinirSenhaRequest;
import com.marcaaiback.model.dto.admin.senha.ValidarTokenRequest;
import com.marcaaiback.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recuperarsenha")
public class AuthSenhaController {

    private final AdminService adminService;

    @PostMapping("/esqueci-senha")
    public ResponseEntity<String> esqueciSenha(@RequestBody @Valid EsqueciSenhaRequest request) {
        try {
            adminService.enviarEmailRedefinicao(request.getEmail());
            return ResponseEntity.ok("Um link foi enviado para seu e-mail.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/validar-token")
    public ResponseEntity<String> validarToken(@RequestBody @Valid ValidarTokenRequest request) {
        try {
            adminService.validarToken(request.getToken());
            return ResponseEntity.ok("Token válido.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<String> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequest request) {
        adminService.redefinirSenha(request.getToken(), request.getNovaSenha());
        return ResponseEntity.ok("Senha redefinida com sucesso!");
    }
}

