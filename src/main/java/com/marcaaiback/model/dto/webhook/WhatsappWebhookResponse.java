package com.marcaaiback.model.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WhatsappWebhookResponse {

    private String telefone;

    private List<String> respostas;

    private boolean encerrarAtendimentoAutomatico;

    public static WhatsappWebhookResponse of(String telefone, List<String> respostas){
        return new WhatsappWebhookResponse(telefone, respostas, false);
    }

    public static WhatsappWebhookResponse atendimentoHumano(String telefone, List<String> respostas){
        return new WhatsappWebhookResponse(telefone, respostas, true);
    }

    public static WhatsappWebhookResponse semResposta() {
        return new WhatsappWebhookResponse(null, List.of(), false);
    }
}
