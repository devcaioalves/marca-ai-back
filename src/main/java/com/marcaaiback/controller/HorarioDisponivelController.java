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

    @PostMapping("/criar-horario")
    public ResponseEntity<HorarioDisponivelResponse> criar(@RequestBody @Valid HorarioDisponivelRequest request) {
        return ResponseEntity.status(201).body(horarioDisponivelService.criar(request));
    }

    @GetMapping("/buscar-horario/{id}")
    public ResponseEntity<HorarioDisponivelResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(horarioDisponivelService.buscarPorId(id));
    }

    @GetMapping("listar-horarios")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarTodos(){
        return ResponseEntity.ok(horarioDisponivelService.listarTodos());
    }

    @GetMapping("/listar-horario-data")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarPorData(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate data) {
        return ResponseEntity.ok(horarioDisponivelService.listarPorData(data));
    }

    @GetMapping("/listar-horario-data/disponiveis")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarDisponiveisPorData(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate data) {
        return ResponseEntity.ok(horarioDisponivelService.listarDisponiveisPorData(data));
    }

    @PatchMapping("/alterar-horario-disponibilidade/{id}")
    public ResponseEntity<HorarioDisponivelResponse> alterarDisponibilidade(@PathVariable Long id,
                                                       @RequestBody @Valid HorarioDisponivelRequest request) {

        return ResponseEntity.ok(horarioDisponivelService.alterarDisponibilidade(id, request));
    }

    @DeleteMapping("/deletar-horario/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        horarioDisponivelService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}