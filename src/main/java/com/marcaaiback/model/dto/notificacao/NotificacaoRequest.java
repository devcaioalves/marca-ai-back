package com.marcaaiback.model.dto.notificacao;

import com.marcaaiback.model.enuns.TipoDeMensagem;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoRequest {

    @NotNull(message = "O tipo da notificação é obrigatório.")
    private TipoDeMensagem tipoDeMensagem;

    @NotNull(message = "O agendamento é obrigatório.")
    private Long agendamentoId;

    // statusNotificacao nasce como ENVIADO automaticamente no service
    // dataEnvio definida automaticamente no service
}