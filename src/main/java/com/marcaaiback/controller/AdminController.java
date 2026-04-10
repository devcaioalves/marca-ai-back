package com.marcaaiback.controller;

import com.marcaaiback.model.dto.admin.AdminRequest;
import com.marcaaiback.model.dto.admin.AdminResponse;
import com.marcaaiback.model.dto.admin.login.LoginRequest;
import com.marcaaiback.model.dto.admin.senha.AlterarSenhaRequest;
import com.marcaaiback.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/criar-admin")
    public ResponseEntity<AdminResponse> criarAdmin(@RequestBody @Valid AdminRequest request) {
        try {
            AdminResponse adminResponse = adminService.criarAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(adminResponse);
        } catch (IllegalArgumentException ex) {
            // Retorna um AdminResponse vazio com a mensagem de erro
            AdminResponse erro = new AdminResponse();
            erro.setMensagemErro(ex.getMessage()); // adicione esse campo no DTO
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    @GetMapping("/buscar-admin")
    public ResponseEntity<AdminResponse> buscar() {
        return ResponseEntity.ok(adminService.buscar());
    }

    @PutMapping("/atualizar-admin")
    public ResponseEntity<AdminResponse> atualizar(@RequestBody @Valid AdminRequest request) {
        return ResponseEntity.ok(adminService.atualizar(request));
    }

    @PatchMapping("/alterar-senha-admin/{id}")
    public ResponseEntity<AdminResponse> alterarSenha(@PathVariable Long id, @RequestBody @Valid AlterarSenhaRequest request) {
        try {
            adminService.alterarSenha(id, request);
            AdminResponse resposta = new AdminResponse();
            resposta.setMensagemErro("Senha alterada com sucesso!");
            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException ex) {
            AdminResponse erro = new AdminResponse();
            erro.setMensagemErro(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            AdminResponse usuario = adminService.autenticar(request);
            return ResponseEntity.ok(usuario);
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            AdminResponse erro = new AdminResponse();
            erro.setMensagemErro(ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
        }
    }
}