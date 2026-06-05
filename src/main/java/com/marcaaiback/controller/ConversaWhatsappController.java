package com.marcaaiback.controller;

import com.marcaaiback.model.dto.conversa.ConversaWhatsappRequest;
import com.marcaaiback.model.dto.conversa.ConversaWhatsappResponse;
import com.marcaaiback.service.ConversaWhatsappService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/conversas")
public class ConversaWhatsappController {

    private final ConversaWhatsappService conversaWhatsappService;

    @PostMapping("/criar-conversa")
    public ResponseEntity<ConversaWhatsappResponse> criar(
            @RequestBody @Valid ConversaWhatsappRequest request
    ) {
        return ResponseEntity
                .status(201)
                .body(conversaWhatsappService.criar(request));
    }

    @GetMapping("/buscar-conversa/{id}")
    public ResponseEntity<ConversaWhatsappResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(conversaWhatsappService.buscarPorId(id));
    }

    @GetMapping("/buscar-por-telefone/{telefone}")
    public ResponseEntity<ConversaWhatsappResponse> buscarPorTelefone(
            @PathVariable String telefone
    ) {
        return ResponseEntity.ok(conversaWhatsappService.buscarPorTelefone(telefone));
    }

    @GetMapping("/listar-conversas")
    public ResponseEntity<List<ConversaWhatsappResponse>> listarTodas() {
        return ResponseEntity.ok(conversaWhatsappService.listarTodos());
    }

    @PutMapping("/atualizar-conversa/{id}")
    public ResponseEntity<ConversaWhatsappResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ConversaWhatsappRequest request
    ) {
        return ResponseEntity.ok(conversaWhatsappService.atualizar(id, request));
    }

    @DeleteMapping("/deletar-conversa/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id
    ) {
        conversaWhatsappService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}