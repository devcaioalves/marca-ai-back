package com.marcaaiback.controller;

import com.marcaaiback.model.dto.mensagem.MensagemRequest;
import com.marcaaiback.model.dto.mensagem.MensagemResponse;
import com.marcaaiback.service.MensagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mensagens")
public class MensagemController {

    private final MensagemService mensagemService;

    @PostMapping("/registrar-mensagem")
    public ResponseEntity<MensagemResponse> registrar(@RequestBody @Valid MensagemRequest request) {
        return ResponseEntity.status(201).body(mensagemService.registrar(request));
    }

    @GetMapping("/listar-mensagem-agendamento/{agendamentoId}")
    public ResponseEntity<List<MensagemResponse>> listarPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(mensagemService.listarPorAgendamento(agendamentoId));
    }

    @GetMapping("/listar-mensagem-cliente/{clienteId}")
    public ResponseEntity<List<MensagemResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(mensagemService.listarPorCliente(clienteId));
    }
}