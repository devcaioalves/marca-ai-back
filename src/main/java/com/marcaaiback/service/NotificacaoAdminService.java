package com.marcaaiback.service;

import com.marcaaiback.model.dto.notificacaoAdmin.NotificacaoAdminRequest;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.ConversaWhatsapp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NotificacaoAdminService {

    private final AdminService adminService;
    private final RestTemplate restTemplate;

    @Value("${n8n.url.notificacao-admin}")
    private String urlN8n;

    public void notificar(ConversaWhatsapp conversa) {
        Admin admin = adminService.buscarEntidade();

        // Remove tudo que não for dígito
        String telefoneAdmin = admin.getTelefone().replaceAll("\\D", "");

        // Garante código do país
        if (!telefoneAdmin.startsWith("55")) {
            telefoneAdmin = "55" + telefoneAdmin;
        }

        NotificacaoAdminRequest request = new NotificacaoAdminRequest(
                telefoneAdmin,
                conversa.getCliente().getNome(),
                conversa.getCliente().getTelefone()
        );

        restTemplate.postForEntity(urlN8n, request, Void.class);
    }
}