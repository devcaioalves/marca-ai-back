package com.marcaaiback.controller;

import com.marcaaiback.model.dto.servico.ServicoRequest;
import com.marcaaiback.model.dto.servico.ServicoResponse;
import com.marcaaiback.service.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@RequestBody @Valid ServicoRequest request) {
        return ResponseEntity.status(201).body(servicoService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listarTodos() {
        return ResponseEntity.ok(servicoService.listarTodos());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ServicoResponse>> listarAtivos() {
        return ResponseEntity.ok(servicoService.listarAtivos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid ServicoRequest request) {
        return ResponseEntity.ok(servicoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativar-desativar")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id) {
        servicoService.ativarDesativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}