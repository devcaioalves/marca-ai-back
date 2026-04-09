package com.marcaaiback.model.dto.notificacao;

import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.model.enuns.TipoDeMensagem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoResponse {

    private Long id;
    private LocalDateTime dataEnvio;
    private StatusNotificacao statusNotificacao;
    private TipoDeMensagem tipoDeMensagem;
    private Long agendamentoId;

    // cliente acessado via agendamento
    private String clienteNome;
    private String clienteTelefone; // necessário para o chatbot saber para quem disparar
}