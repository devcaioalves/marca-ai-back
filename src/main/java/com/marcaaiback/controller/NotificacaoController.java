package com.marcaaiback.controller;

import com.marcaaiback.model.dto.notificacao.NotificacaoRequest;
import com.marcaaiback.model.dto.notificacao.NotificacaoResponse;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.service.NotificacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PostMapping("/disparar-notificacao")
    public ResponseEntity<NotificacaoResponse> disparar(@RequestBody @Valid NotificacaoRequest request) {
        return ResponseEntity.status(201).body(notificacaoService.disparar(request));
    }

    @GetMapping("/listar-notificacao-agendamento/{agendamentoId}")
    public ResponseEntity<List<NotificacaoResponse>> listarPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(notificacaoService.listarPorAgendamento(agendamentoId));
    }

    @GetMapping("/listar-notificacoes-agendamentos")
    public ResponseEntity<List<NotificacaoResponse>> listarTodasPorAgendamento() {
        return ResponseEntity.ok(notificacaoService.listarTodasPorAgendamento());
    }

    @GetMapping("/listar-notificacao-status/{status}")
    public ResponseEntity<List<NotificacaoResponse>> listarPorStatus(@PathVariable StatusNotificacao status) {
        return ResponseEntity.ok(notificacaoService.listarPorStatus(status));
    }

    @PatchMapping("/marcar-notificacao-lida/{id}")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.noContent().build();
    }
}