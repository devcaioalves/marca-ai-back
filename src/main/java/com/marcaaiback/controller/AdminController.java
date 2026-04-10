package com.marcaaiback.controller;

import com.marcaaiback.model.dto.admin.AdminRequest;
import com.marcaaiback.model.dto.admin.AdminResponse;
import com.marcaaiback.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<AdminResponse> buscar() {
        return ResponseEntity.ok(adminService.buscar());
    }

    @PutMapping
    public ResponseEntity<AdminResponse> atualizar(@RequestBody @Valid AdminRequest request) {
        return ResponseEntity.ok(adminService.atualizar(request));
    }
}