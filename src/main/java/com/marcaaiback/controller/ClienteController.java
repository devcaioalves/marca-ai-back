package com.marcaaiback.controller;

import com.marcaaiback.model.dto.cliente.ClienteRequest;
import com.marcaaiback.model.dto.cliente.ClienteResponse;
import com.marcaaiback.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping("/criar-cliente")
    public ResponseEntity<ClienteResponse> criar(@RequestBody @Valid ClienteRequest request) {
        return ResponseEntity.status(201).body(clienteService.criar(request));
    }

    @GetMapping("/buscar-cliente/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/buscar-por-nome-ou-telefone/{termo}")
    public ResponseEntity<List<ClienteResponse>> buscarPorNomeOuTelefone(@PathVariable String termo) {
        return ResponseEntity.ok(clienteService.buscarPorNomeOuTelefone(termo));
    }

    @GetMapping("/buscar-pelo-telefone/{telefone}")
    public ResponseEntity<ClienteResponse> buscarPorTelefone(@PathVariable String telefone) {
        System.out.println("Telefone recebido: " + telefone);
        return ResponseEntity.ok(clienteService.buscarPorTelefone(telefone));
    }

    @GetMapping("/listar-clientes")
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @PutMapping("/atualizar-cliente/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("deletar-cliente/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}