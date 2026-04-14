package com.marcaaiback.controller;

import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.enuns.StatusAgendamento;
import com.marcaaiback.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping("/criar-agendamento")
    public ResponseEntity<AgendamentoResponse> criar(@RequestBody @Valid AgendamentoRequest request) {
        return ResponseEntity.status(201).body(agendamentoService.criar(request));
    }

    @GetMapping("/buscar-agendamento/{id}")
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @GetMapping("/listar-por-data/{data}")
    public ResponseEntity<List<AgendamentoResponse>> listarPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendamentoService.listarPorData(data));
    }

    @GetMapping("/listar-por-cliente/{clienteId}")
    public ResponseEntity<List<AgendamentoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(agendamentoService.listarPorCliente(clienteId));
    }

    @GetMapping("/listar-por-status/{status}")
    public ResponseEntity<List<AgendamentoResponse>> listarPorStatus(@PathVariable StatusAgendamento status) {
        return ResponseEntity.ok(agendamentoService.listarPorStatus(status));
    }

    @PatchMapping("/remarcar-agendamento/{id}/{novoHorarioId}")
    public ResponseEntity<AgendamentoResponse> remarcar(@PathVariable Long id, @PathVariable Long novoHorarioId) {
        return ResponseEntity.ok(agendamentoService.remarcar(id, novoHorarioId));
    }

    @PatchMapping("/confirmar-agendamento/{id}")
    public ResponseEntity<AgendamentoResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.confirmar(id));
    }

    @PatchMapping("/cancelar-agendamento/{id}")
    public ResponseEntity<AgendamentoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.cancelar(id));
    }

    @PatchMapping("/realizar-agendamento/{id}")
    public ResponseEntity<AgendamentoResponse> realizar(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.realizarAtendimento(id));
    }
}