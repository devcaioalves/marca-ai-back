package com.marcaaiback.controller;

import com.marcaaiback.model.dto.webhook.WhatsappWebhookRequest;
import com.marcaaiback.model.dto.webhook.WhatsappWebhookResponse;
import com.marcaaiback.service.chatbot.WhatsappChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsappWebhookController {

    private final WhatsappChatbotService chatbotService;

    @PostMapping("/messages")
    public ResponseEntity<WhatsappWebhookResponse> receberMensagem(@RequestBody @Valid WhatsappWebhookRequest request) {
        return ResponseEntity.ok(chatbotService.processarMensagem(request));
    }
}
