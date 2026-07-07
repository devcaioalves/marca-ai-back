package com.marcaaiback.model.dto.webhook;

import com.marcaaiback.model.enuns.TipoMensagemWhatsapp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WhatsappWebhookRequest {

    @NotNull(message = "O nome do cliente é obrigatório.")
    private String nomeCliente;

    @NotBlank(message = "O telefone do cliente é obrigatório.")
    private String telefoneCliente;

    @NotNull(message = "A mensagem é obrigatória.")
    private String mensagemWhatsapp;

    private String mensagemId;

    private TipoMensagemWhatsapp tipoMensagemWhatsapp;

    private Boolean fromMe;
}
