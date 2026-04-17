package com.marcaaiback.controller;

import com.marcaaiback.model.dto.admin.AdminRequest;
import com.marcaaiback.model.dto.admin.AdminResponse;
import com.marcaaiback.model.dto.admin.AuthResponse;
import com.marcaaiback.model.dto.admin.login.LoginRequest;
import com.marcaaiback.model.dto.admin.senha.AlterarSenhaRequest;
import com.marcaaiback.service.AdminService;
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
    public ResponseEntity<AdminResponse> criarAdmin(@RequestBody @Valid AdminRequest adminRequest) {
        AdminResponse adminResponse = adminService.criarAdmin(adminRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminResponse);
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
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id, @RequestBody @Valid AlterarSenhaRequest request) {
        adminService.alterarSenha(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse usuario = adminService.autenticar(request);
        return ResponseEntity.ok(usuario);
    }
}