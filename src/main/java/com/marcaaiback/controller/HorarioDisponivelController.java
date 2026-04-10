package com.marcaaiback.controller;

import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelRequest;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.service.HorarioDisponivelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/horarios")
public class HorarioDisponivelController {

    private final HorarioDisponivelService horarioDisponivelService;

    @PostMapping
    public ResponseEntity<HorarioDisponivelResponse> criar(@RequestBody @Valid HorarioDisponivelRequest request) {
        return ResponseEntity.status(201).body(horarioDisponivelService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioDisponivelResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(horarioDisponivelService.buscarPorId(id));
    }

    @GetMapping("/data/{data}")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(horarioDisponivelService.listarPorData(data));
    }

    @GetMapping("/data/{data}/disponiveis")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarDisponiveisPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(horarioDisponivelService.listarDisponiveisPorData(data));
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> alterarDisponibilidade(@PathVariable Long id) {
        horarioDisponivelService.alterarDisponibilidade(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        horarioDisponivelService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}