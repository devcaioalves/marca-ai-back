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

    @PostMapping("/criar-servico")
    public ResponseEntity<ServicoResponse> criar(@RequestBody @Valid ServicoRequest request) {
        return ResponseEntity.status(201).body(servicoService.criar(request));
    }

    @GetMapping("/buscar-servico/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @GetMapping("/listar-servicos")
    public ResponseEntity<List<ServicoResponse>> listarTodos() {
        return ResponseEntity.ok(servicoService.listarTodos());
    }

    @GetMapping("/listar-servicos/ativos")
    public ResponseEntity<List<ServicoResponse>> listarAtivos() {
        return ResponseEntity.ok(servicoService.listarAtivos());
    }

    @PutMapping("/atualizar-servico/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid ServicoRequest request) {
        return ResponseEntity.ok(servicoService.atualizar(id, request));
    }

    @PatchMapping("/ativar-desativar-servico/{id}")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id) {
        servicoService.ativarDesativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deletar-servico/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}